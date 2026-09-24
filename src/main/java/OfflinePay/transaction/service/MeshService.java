package OfflinePay.transaction.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import OfflinePay.transaction.dto.MeshPacket;

@Service
public class MeshService {

    private final List<MeshPacket> packets =
            new ArrayList<>();

    private final Set<String> receivedPacketIds =
            new HashSet<>();

    public synchronized MeshPacket receivePacket(
            MeshPacket packet) {

        if (packet == null) {
            throw new IllegalArgumentException(
                    "Mesh packet is required"
            );
        }

        if (packet.getPacketId() == null ||
                packet.getPacketId().isBlank()) {

            throw new IllegalArgumentException(
                    "Packet ID is required"
            );
        }

        // Duplicate packet
        if (receivedPacketIds.contains(
                packet.getPacketId())) {

            return packet;
        }

        // Packet cannot be relayed after TTL expires
        if (packet.getTtl() <= 0) {
            return packet;
        }

        // Increase hop count at this relay
        packet.setHopCount(
                packet.getHopCount() + 1
        );

        // Consume one TTL hop
        packet.setTtl(
                packet.getTtl() - 1
        );

        packets.add(packet);

        receivedPacketIds.add(
                packet.getPacketId()
        );

        return packet;
    }

    public synchronized List<MeshPacket> getPackets() {
        return new ArrayList<>(packets);
    }

    public synchronized void clearPackets() {
        packets.clear();
        receivedPacketIds.clear();
    }
}