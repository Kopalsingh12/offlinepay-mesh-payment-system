package OfflinePay.transaction.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import OfflinePay.transaction.dto.MeshPacket;

@Service
public class MeshService {

    private final List<MeshPacket> packets = new ArrayList<>();

    private final Set<String> receivedPacketIds = new HashSet<>();

    public MeshPacket receivePacket(MeshPacket packet) {

        // Prevent duplicate packets
        if (receivedPacketIds.contains(packet.getPacketId())) {
            return packet;
        }

        // Increase hop count when packet reaches a new relay
        packet.setHopCount(packet.getHopCount() + 1);

        packets.add(packet);
        receivedPacketIds.add(packet.getPacketId());

        return packet;
    }

    public List<MeshPacket> getPackets() {
        return packets;
    }

    public void clearPackets() {
        packets.clear();
        receivedPacketIds.clear();
    }
}