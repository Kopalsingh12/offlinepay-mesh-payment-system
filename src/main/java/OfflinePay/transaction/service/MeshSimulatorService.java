package OfflinePay.transaction.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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

        devices.put("DEVICE004",
                new VirtualDevice("DEVICE004", false));

        devices.put("DEVICE005",
                new VirtualDevice("DEVICE005", false));

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

    public int gossip() {

        int transfers = 0;

        List<VirtualDevice> deviceList =
                new ArrayList<>(devices.values());

        Map<String, List<MeshPacket>> snapshot =
                new LinkedHashMap<>();

        for (VirtualDevice device : deviceList) {

            snapshot.put(
                    device.getDeviceId(),
                    new ArrayList<>(
                            device.getHeldPackets()
                    )
            );
        }

        for (VirtualDevice source : deviceList) {

            List<MeshPacket> packets =
                    snapshot.get(source.getDeviceId());

            for (MeshPacket packet : packets) {

                if (packet.getHopCount() >= packet.getTtl()) {
                    continue;
                }

                for (VirtualDevice target : deviceList) {

                    if (source == target) {
                        continue;
                    }

                    if (target.holds(packet.getPacketId())) {
                        continue;
                    }

                    MeshPacket copy =
                            new MeshPacket(
                                    packet.getPacketId(),
                                    packet.getSenderDeviceId(),
                                    packet.getReceiverDeviceId(),
                                    packet.getPayload(),
                                    packet.getHopCount() + 1,
                                    packet.getTtl()
                            );

                    target.hold(copy);
                    transfers++;
                }
            }
        }

        return transfers;
    }

    public List<BridgeUpload> collectBridgeUploads() {

        List<BridgeUpload> uploads =
                new ArrayList<>();

        for (VirtualDevice device : devices.values()) {

            if (!device.hasInternet()) {
                continue;
            }

            for (MeshPacket packet :
                    device.getHeldPackets()) {

                uploads.add(
                        new BridgeUpload(
                                device.getDeviceId(),
                                packet
                        )
                );
            }
        }

        return uploads;
    }

    public void reset() {

        for (VirtualDevice device :
                devices.values()) {

            device.clear();
        }
    }

    public record BridgeUpload(
            String bridgeNodeId,
            MeshPacket packet) {
    }
}
