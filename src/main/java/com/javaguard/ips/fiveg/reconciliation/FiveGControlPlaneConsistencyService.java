package com.javaguard.ips.fiveg.reconciliation;

import com.javaguard.ips.fiveg.model.SliceId;
import com.javaguard.ips.fiveg.open5gs.AmfUeRegistry;
import com.javaguard.ips.fiveg.open5gs.ControlPlaneSessionRegistry;
import com.javaguard.ips.fiveg.open5gs.GnbRegistry;
import com.javaguard.ips.fiveg.open5gs.model.AmfUeContext;
import com.javaguard.ips.fiveg.open5gs.model.ControlPlanePduSession;

import org.springframework.stereotype.Service;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FiveGControlPlaneConsistencyService {

    private final ControlPlaneSessionRegistry smfRegistry;
    private final AmfUeRegistry amfUeRegistry;
    private final GnbRegistry gnbRegistry;


    public FiveGControlPlaneConsistencyService(
            ControlPlaneSessionRegistry smfRegistry,
            AmfUeRegistry amfUeRegistry,
            GnbRegistry gnbRegistry
    ) {
        this.smfRegistry = smfRegistry;
        this.amfUeRegistry = amfUeRegistry;
        this.gnbRegistry = gnbRegistry;
    }


    public FiveGControlPlaneConsistencySnapshot analyze() {

        List<ControlPlanePduSession> sessions =
                smfRegistry.findAll();

        List<AmfUeContext> ues =
                amfUeRegistry.findAll();

        List<FiveGControlPlaneConsistencySnapshot.Issue> issues =
                new ArrayList<>();


        int matchedSessions = 0;
        int sessionsWithoutAmf = 0;
        int unknownGnbs = 0;
        int sliceMismatches = 0;


        for (ControlPlanePduSession session : sessions) {

            Optional<AmfUeContext> ueResult =
                    amfUeRegistry.findBySupi(
                            session.supi()
                    );

            if (ueResult.isEmpty()) {

                sessionsWithoutAmf++;

                issues.add(
                        new FiveGControlPlaneConsistencySnapshot.Issue(
                                FiveGControlPlaneConsistencySnapshot
                                        .IssueType
                                        .SESSION_WITHOUT_AMF_REGISTRATION,
                                session.supi(),
                                session.pduSessionId(),
                                null,
                                "SMF PDU session exists, but AMF has no UE registration for this SUPI"
                        )
                );

                continue;
            }

            matchedSessions++;

            AmfUeContext ue =
                    ueResult.get();

            if (isSliceMismatch(session.slice(), ue)) {

                sliceMismatches++;

                issues.add(
                        new FiveGControlPlaneConsistencySnapshot.Issue(
                                FiveGControlPlaneConsistencySnapshot
                                        .IssueType
                                        .SLICE_MISMATCH,
                                session.supi(),
                                session.pduSessionId(),
                                ue.gnbId(),
                                "SMF PDU session uses slice "
                                        + session.slice().displayName()
                                        + ", but AMF allowed slices are "
                                        + formatSlices(
                                        ue.allowedSlices()
                                )
                        )
                );
            }
        }


        for (AmfUeContext ue : ues) {

            if (ue.gnbId() == null) {
                continue;
            }

            if (
                    gnbRegistry
                            .findById(
                                    ue.gnbId()
                            )
                            .isPresent()
            ) {
                continue;
            }

            unknownGnbs++;

            issues.add(
                    new FiveGControlPlaneConsistencySnapshot.Issue(
                            FiveGControlPlaneConsistencySnapshot
                                    .IssueType
                                    .UNKNOWN_GNB,
                            ue.supi(),
                            null,
                            ue.gnbId(),
                            "AMF UE context references gNB "
                                    + ue.gnbId()
                                    + ", but this gNB is absent from /gnb-info"
                    )
            );
        }


        return new FiveGControlPlaneConsistencySnapshot(
                ues.size(),
                sessions.size(),
                gnbRegistry.size(),
                matchedSessions,
                sessionsWithoutAmf,
                unknownGnbs,
                sliceMismatches,
                List.copyOf(issues),
                Instant.now()
        );
    }


    private boolean isSliceMismatch(
            SliceId sessionSlice,
            AmfUeContext ue
    ) {


        if (ue.allowedSlices().isEmpty()) {
            return false;
        }

        return !ue.allowsSlice(
                sessionSlice
        );
    }


    private String formatSlices(
            List<SliceId> slices
    ) {

        return slices
                .stream()
                .map(SliceId::displayName)
                .toList()
                .toString();
    }
}