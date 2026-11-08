package com.javaguard.ips.flow.tcp;

import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TcpFlowTracker {

    private static final Duration HALF_OPEN_TTL =
            Duration.ofMinutes(2);

    private static final Duration ESTABLISHED_IDLE_TTL =
            Duration.ofMinutes(30);

    private static final Duration TERMINAL_TTL =
            Duration.ofMinutes(5);


    private final ConcurrentMap<UUID, MutableTcpFlow> flows =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<TcpTuple, UUID> tupleIndex =
            new ConcurrentHashMap<>();


    public synchronized Optional<TcpFlowSnapshot> track(
            NetworkEvent event
    ) {

        if (!isTrackableTcp(event)) {
            return Optional.empty();
        }

        TcpTuple tuple =
                new TcpTuple(
                        event.sourceIp(),
                        event.sourcePort(),
                        event.destinationIp(),
                        event.destinationPort()
                );

        UUID existingId =
                tupleIndex.get(tuple);

        if (existingId != null) {

            MutableTcpFlow existing =
                    flows.get(existingId);

            if (existing != null) {

                boolean newConnection =
                        existing.isTerminal()
                                && event.tcpSyn()
                                && !event.tcpAck();

                if (!newConnection) {

                    boolean fromClient =
                            tuple.equals(
                                    existing.clientTuple()
                            );

                    existing.update(
                            event,
                            fromClient
                    );

                    return Optional.of(
                            existing.snapshot()
                    );
                }

                removeFlow(existing);
            }
        }


        /*
         * Новый flow создаём только если увидели
         * начальный SYN клиента.
         */
        if (!event.tcpSyn() || event.tcpAck()) {
            return Optional.empty();
        }


        MutableTcpFlow flow =
                new MutableTcpFlow(
                        UUID.randomUUID(),
                        new TcpFlowKey(
                                event.sourceIp(),
                                event.sourcePort(),
                                event.destinationIp(),
                                event.destinationPort()
                        ),
                        event.timestamp()
                );


        registerFlow(flow);

        flow.update(
                event,
                true
        );


        return Optional.of(
                flow.snapshot()
        );
    }


    public Optional<TcpFlowSnapshot> find(
            NetworkEvent event
    ) {

        if (!isTrackableTcp(event)) {
            return Optional.empty();
        }

        TcpTuple tuple =
                new TcpTuple(
                        event.sourceIp(),
                        event.sourcePort(),
                        event.destinationIp(),
                        event.destinationPort()
                );

        UUID id =
                tupleIndex.get(tuple);

        if (id == null) {
            return Optional.empty();
        }

        MutableTcpFlow flow =
                flows.get(id);

        if (flow == null) {
            return Optional.empty();
        }

        return Optional.of(
                flow.snapshot()
        );
    }


    public int countHalfOpenByClient(
            String clientIp,
            Instant now,
            Duration window
    ) {

        Instant cutoff =
                now.minus(window);

        return (int) flows
                .values()
                .stream()
                .map(MutableTcpFlow::snapshot)
                .filter(
                        flow ->
                                flow.key()
                                        .clientIp()
                                        .equals(clientIp)
                )
                .filter(
                        flow ->
                                !flow.createdAt()
                                        .isBefore(cutoff)
                )
                .filter(
                        TcpFlowSnapshot::isHalfOpen
                )
                .count();
    }


    public int countRecentResetsByClient(
            String clientIp,
            Instant now,
            Duration window
    ) {

        Instant cutoff =
                now.minus(window);

        return (int) flows
                .values()
                .stream()
                .map(MutableTcpFlow::snapshot)
                .filter(
                        flow ->
                                flow.key()
                                        .clientIp()
                                        .equals(clientIp)
                )
                .filter(
                        flow ->
                                flow.state()
                                        == TcpFlowState.RESET
                )
                .filter(
                        flow ->
                                !flow.lastSeenAt()
                                        .isBefore(cutoff)
                )
                .count();
    }


    public TcpClientFlowStats getClientStats(
            String clientIp,
            Instant now,
            Duration window
    ) {

        Instant cutoff =
                now.minus(window);

        List<TcpFlowSnapshot> recent =
                flows
                        .values()
                        .stream()
                        .map(MutableTcpFlow::snapshot)
                        .filter(
                                flow ->
                                        flow.key()
                                                .clientIp()
                                                .equals(clientIp)
                        )
                        .filter(
                                flow ->
                                        !flow.createdAt()
                                                .isBefore(cutoff)
                        )
                        .toList();


        int total =
                recent.size();

        int established =
                (int) recent
                        .stream()
                        .filter(
                                TcpFlowSnapshot::isEstablished
                        )
                        .count();

        int halfOpen =
                (int) recent
                        .stream()
                        .filter(
                                TcpFlowSnapshot::isHalfOpen
                        )
                        .count();

        int resets =
                (int) recent
                        .stream()
                        .filter(
                                flow ->
                                        flow.state()
                                                == TcpFlowState.RESET
                        )
                        .count();

        int closed =
                (int) recent
                        .stream()
                        .filter(
                                flow ->
                                        flow.state()
                                                == TcpFlowState.CLOSED
                        )
                        .count();


        int failed =
                halfOpen + resets;

        double failureRatio =
                total == 0
                        ? 0.0
                        : (double) failed / total;


        return new TcpClientFlowStats(
                clientIp,
                total,
                established,
                halfOpen,
                resets,
                closed,
                failureRatio
        );
    }


    public TcpFlowSummary summary() {

        List<TcpFlowSnapshot> snapshots =
                flows
                        .values()
                        .stream()
                        .map(MutableTcpFlow::snapshot)
                        .toList();


        return new TcpFlowSummary(
                snapshots.size(),

                countState(
                        snapshots,
                        TcpFlowState.SYN_SENT,
                        TcpFlowState.SYN_ACK_SEEN
                ),

                countState(
                        snapshots,
                        TcpFlowState.ESTABLISHED
                ),

                countState(
                        snapshots,
                        TcpFlowState.CLOSING
                ),

                countState(
                        snapshots,
                        TcpFlowState.CLOSED
                ),

                countState(
                        snapshots,
                        TcpFlowState.RESET
                )
        );
    }


    public List<TcpFlowSnapshot> findRecent(
            int limit
    ) {

        int safeLimit =
                Math.max(
                        1,
                        Math.min(
                                limit,
                                500
                        )
                );

        return flows
                .values()
                .stream()
                .map(MutableTcpFlow::snapshot)
                .sorted(
                        Comparator
                                .comparing(
                                        TcpFlowSnapshot::lastSeenAt
                                )
                                .reversed()
                )
                .limit(safeLimit)
                .toList();
    }


    @Scheduled(fixedDelay = 60_000)
    public synchronized void cleanup() {

        Instant now =
                Instant.now();

        List<MutableTcpFlow> expired =
                flows
                        .values()
                        .stream()
                        .filter(
                                flow ->
                                        isExpired(
                                                flow.snapshot(),
                                                now
                                        )
                        )
                        .toList();

        for (MutableTcpFlow flow : expired) {
            removeFlow(flow);
        }
    }


    private boolean isTrackableTcp(
            NetworkEvent event
    ) {

        return event != null
                && event.protocol() == NetworkProtocol.TCP
                && event.sourcePort() != null
                && event.destinationPort() != null;
    }


    private boolean isExpired(
            TcpFlowSnapshot flow,
            Instant now
    ) {

        Duration idle =
                Duration.between(
                        flow.lastSeenAt(),
                        now
                );

        if (flow.isTerminal()) {
            return idle.compareTo(
                    TERMINAL_TTL
            ) > 0;
        }

        if (flow.isHalfOpen()) {
            return idle.compareTo(
                    HALF_OPEN_TTL
            ) > 0;
        }

        return idle.compareTo(
                ESTABLISHED_IDLE_TTL
        ) > 0;
    }


    private void registerFlow(
            MutableTcpFlow flow
    ) {

        flows.put(
                flow.id(),
                flow
        );

        tupleIndex.put(
                flow.clientTuple(),
                flow.id()
        );

        tupleIndex.put(
                flow.serverTuple(),
                flow.id()
        );
    }


    private void removeFlow(
            MutableTcpFlow flow
    ) {

        flows.remove(
                flow.id(),
                flow
        );

        tupleIndex.remove(
                flow.clientTuple(),
                flow.id()
        );

        tupleIndex.remove(
                flow.serverTuple(),
                flow.id()
        );
    }


    private int countState(
            List<TcpFlowSnapshot> flows,
            TcpFlowState... states
    ) {

        return (int) flows
                .stream()
                .filter(
                        flow -> {

                            for (TcpFlowState state : states) {

                                if (flow.state() == state) {
                                    return true;
                                }
                            }

                            return false;
                        }
                )
                .count();
    }


    private record TcpTuple(
            String sourceIp,
            int sourcePort,
            String destinationIp,
            int destinationPort
    ) {
    }


    private static class MutableTcpFlow {

        private final UUID id;

        private final TcpFlowKey key;

        private final Instant createdAt;


        private TcpFlowState state =
                TcpFlowState.SYN_SENT;

        private Instant lastSeenAt;

        private Instant establishedAt;

        private Instant closedAt;


        private long packets;

        private long bytes;

        private long clientPackets;

        private long serverPackets;

        private long synPackets;

        private long rstPackets;

        private long finPackets;


        private boolean clientFinSeen;

        private boolean serverFinSeen;


        private MutableTcpFlow(
                UUID id,
                TcpFlowKey key,
                Instant createdAt
        ) {
            this.id = id;
            this.key = key;
            this.createdAt = createdAt;
            this.lastSeenAt = createdAt;
        }


        private synchronized void update(
                NetworkEvent event,
                boolean fromClient
        ) {

            packets++;

            bytes += event.packetSize();

            lastSeenAt = event.timestamp();


            if (fromClient) {
                clientPackets++;

            } else {
                serverPackets++;
            }


            if (event.tcpSyn()) {
                synPackets++;
            }

            if (event.tcpRst()) {
                rstPackets++;

                state = TcpFlowState.RESET;

                closedAt = event.timestamp();

                return;
            }


            if (state == TcpFlowState.SYN_SENT
                    && !fromClient
                    && event.tcpSyn()
                    && event.tcpAck()) {

                state = TcpFlowState.SYN_ACK_SEEN;

                return;
            }


            if (state == TcpFlowState.SYN_ACK_SEEN
                    && fromClient
                    && event.tcpAck()
                    && !event.tcpSyn()) {

                state = TcpFlowState.ESTABLISHED;

                establishedAt =
                        event.timestamp();
            }


            if (event.tcpFin()) {

                finPackets++;

                if (fromClient) {
                    clientFinSeen = true;

                } else {
                    serverFinSeen = true;
                }

                if (clientFinSeen && serverFinSeen) {

                    state =
                            TcpFlowState.CLOSED;

                    closedAt =
                            event.timestamp();

                } else {
                    state =
                            TcpFlowState.CLOSING;
                }
            }
        }


        private UUID id() {
            return id;
        }


        private boolean isTerminal() {
            return state == TcpFlowState.CLOSED
                    || state == TcpFlowState.RESET;
        }


        private TcpTuple clientTuple() {

            return new TcpTuple(
                    key.clientIp(),
                    key.clientPort(),
                    key.serverIp(),
                    key.serverPort()
            );
        }


        private TcpTuple serverTuple() {

            return new TcpTuple(
                    key.serverIp(),
                    key.serverPort(),
                    key.clientIp(),
                    key.clientPort()
            );
        }


        private synchronized TcpFlowSnapshot snapshot() {

            return new TcpFlowSnapshot(
                    id,
                    key,
                    state,
                    createdAt,
                    lastSeenAt,
                    establishedAt,
                    closedAt,
                    packets,
                    bytes,
                    clientPackets,
                    serverPackets,
                    synPackets,
                    rstPackets,
                    finPackets
            );
        }
    }
}