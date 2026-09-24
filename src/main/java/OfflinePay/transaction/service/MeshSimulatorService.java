package OfflinePay.transaction.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import OfflinePay.transaction.dto.MeshPacket;

@Service
public class MeshSimulatorService {

    private final Map<String, VirtualDevice> devices =
            new LinkedHashMap<>();

    public MeshSimulatorService() {
        devices.put("DEVICE001",
                new VirtualDevice("DEVICE001", false));

        devices.put("DEVICE002",
                new VirtualDevice("DEVICE002", false));

        devices.put("DEVICE003",
                new VirtualDevice("DEVICE003", true));
    }

    public Map<String, VirtualDevice> getDevices() {
        return devices;
    }

    public void inject(MeshPacket packet) {
        VirtualDevice sender =
                devices.get(packet.getSenderDeviceId());

        if (sender == null) {
            throw new IllegalArgumentException(
                    "Unknown sender device: " +
                    packet.getSenderDeviceId()
            );
        }

        sender.hold(packet);
    }

    public void gossip() {
        for (VirtualDevice device : devices.values()) {

            for (MeshPacket packet : device.getHeldPackets()) {

                VirtualDevice receiver =
                        devices.get(packet.getReceiverDeviceId());

                if (receiver != null) {
                    receiver.hold(packet);
                }

                if (packet.getHopCount() < packet.getTtl()) {
                    packet.setHopCount(
                            packet.getHopCount() + 1
                    );
                }
            }
        }
    }

    public void reset() {
        for (VirtualDevice device : devices.values()) {
            device.clear();
        }
    }
}
