package OfflinePay.transaction.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import OfflinePay.transaction.dto.MeshPacket;

@Service
public class MeshService {

    private final List<MeshPacket> packets = new ArrayList<>();

    public MeshPacket receivePacket(MeshPacket packet) {

        packet.setHopCount(packet.getHopCount() + 1);

        packets.add(packet);

        return packet;
    }

    public List<MeshPacket> getPackets() {
        return packets;
    }

    public void clearPackets() {
        packets.clear();
    }
}
