package com.javaguard.ips.fiveg.reconciliation;

import com.javaguard.ips.fiveg.model.SliceId;
import com.javaguard.ips.fiveg.open5gs.AmfUeRegistry;
import com.javaguard.ips.fiveg.open5gs.ControlPlaneSessionRegistry;
import com.javaguard.ips.fiveg.open5gs.GnbRegistry;

import com.javaguard.ips.fiveg.open5gs.model.AmfUeContext;
import com.javaguard.ips.fiveg.open5gs.model.ControlPlanePduSession;
import com.javaguard.ips.fiveg.open5gs.model.GnbContext;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiveGControlPlaneConsistencyServiceTest {

    @Test
    void shouldMatchValidAmfAndSmfSession() {

        ControlPlaneSessionRegistry smf =
                new ControlPlaneSessionRegistry();

        AmfUeRegistry amf =
                new AmfUeRegistry();

        GnbRegistry gnbs =
                new GnbRegistry();


        SliceId slice =
                new SliceId(
                        1,
                        "010203"
                );


        smf.replaceAll(
                List.of(
                        new ControlPlanePduSession(
                                "imsi-001010000000001",
                                1,
                                "10.45.0.2",
                                "internet",
                                slice,
                                "active",
                                "active",
                                Instant.now()
                        )
                )
        );


        amf.replaceAll(
                List.of(
                        new AmfUeContext(
                                "imsi-001010000000001",
                                "connected",
                                100L,
                                1L,
                                List.of(slice),
                                List.of(slice),
                                Instant.now()
                        )
                )
        );


        gnbs.replaceAll(
                List.of(
                        new GnbContext(
                                100L,
                                "00101",
                                "[192.168.1.100]:38412",
                                true,
                                1,
                                List.of(slice),
                                Instant.now()
                        )
                )
        );


        FiveGControlPlaneConsistencyService service =
                new FiveGControlPlaneConsistencyService(
                        smf,
                        amf,
                        gnbs
                );


        FiveGControlPlaneConsistencySnapshot result =
                service.analyze();


        assertEquals(
                1,
                result.matchedSessions()
        );

        assertEquals(
                0,
                result.sessionsWithoutAmfRegistration()
        );

        assertEquals(
                0,
                result.uesWithUnknownGnb()
        );

        assertEquals(
                0,
                result.sliceMismatches()
        );

        assertEquals(
                0,
                result.issues().size()
        );
    }


    @Test
    void shouldDetectSmfSessionWithoutAmfRegistration() {

        ControlPlaneSessionRegistry smf =
                new ControlPlaneSessionRegistry();

        AmfUeRegistry amf =
                new AmfUeRegistry();

        GnbRegistry gnbs =
                new GnbRegistry();


        smf.replaceAll(
                List.of(
                        new ControlPlanePduSession(
                                "imsi-001010000000002",
                                1,
                                "10.45.0.3",
                                "internet",
                                new SliceId(
                                        1,
                                        "010203"
                                ),
                                "active",
                                "active",
                                Instant.now()
                        )
                )
        );


        FiveGControlPlaneConsistencyService service =
                new FiveGControlPlaneConsistencyService(
                        smf,
                        amf,
                        gnbs
                );


        FiveGControlPlaneConsistencySnapshot result =
                service.analyze();


        assertEquals(
                1,
                result.sessionsWithoutAmfRegistration()
        );

        assertEquals(
                FiveGControlPlaneConsistencySnapshot
                        .IssueType
                        .SESSION_WITHOUT_AMF_REGISTRATION,
                result.issues()
                        .getFirst()
                        .type()
        );
    }


    @Test
    void shouldDetectSliceMismatch() {

        ControlPlaneSessionRegistry smf =
                new ControlPlaneSessionRegistry();

        AmfUeRegistry amf =
                new AmfUeRegistry();

        GnbRegistry gnbs =
                new GnbRegistry();


        SliceId sessionSlice =
                new SliceId(
                        1,
                        "010203"
                );

        SliceId allowedSlice =
                new SliceId(
                        1,
                        "FFFFFF"
                );


        smf.replaceAll(
                List.of(
                        new ControlPlanePduSession(
                                "imsi-001010000000003",
                                1,
                                "10.45.0.4",
                                "internet",
                                sessionSlice,
                                "active",
                                "active",
                                Instant.now()
                        )
                )
        );


        amf.replaceAll(
                List.of(
                        new AmfUeContext(
                                "imsi-001010000000003",
                                "connected",
                                null,
                                null,
                                List.of(
                                        allowedSlice
                                ),
                                List.of(
                                        allowedSlice
                                ),
                                Instant.now()
                        )
                )
        );


        FiveGControlPlaneConsistencyService service =
                new FiveGControlPlaneConsistencyService(
                        smf,
                        amf,
                        gnbs
                );


        FiveGControlPlaneConsistencySnapshot result =
                service.analyze();


        assertEquals(
                1,
                result.sliceMismatches()
        );

        assertEquals(
                FiveGControlPlaneConsistencySnapshot
                        .IssueType
                        .SLICE_MISMATCH,
                result.issues()
                        .getFirst()
                        .type()
        );
    }
}