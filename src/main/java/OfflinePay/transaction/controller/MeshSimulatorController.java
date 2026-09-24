package OfflinePay.transaction.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import OfflinePay.bridge.BridgeIngestionService;
import OfflinePay.transaction.dto.MeshPacket;
import OfflinePay.transaction.service.MeshSimulatorService;
import OfflinePay.transaction.service.VirtualDevice;

@RestController
@RequestMapping("/api/mesh")
public class MeshSimulatorController {

    private final MeshSimulatorService meshSimulatorService;
    private final BridgeIngestionService bridgeIngestionService;

    public MeshSimulatorController(
            MeshSimulatorService meshSimulatorService,
            BridgeIngestionService bridgeIngestionService) {

        this.meshSimulatorService =
                meshSimulatorService;

        this.bridgeIngestionService =
                bridgeIngestionService;
    }

    @GetMapping("/state")
    public Map<String, Object> state() {

        Map<String, Object> result =
                new LinkedHashMap<>();

        for (Map.Entry<String, VirtualDevice> entry :
                meshSimulatorService.getDevices().entrySet()) {

            VirtualDevice device =
                    entry.getValue();

            Map<String, Object> deviceState =
                    new LinkedHashMap<>();

            deviceState.put(
                    "deviceId",
                    device.getDeviceId()
            );

            deviceState.put(
                    "hasInternet",
                    device.hasInternet()
            );

            deviceState.put(
                    "packetCount",
                    device.packetCount()
            );

            result.put(
                    entry.getKey(),
                    deviceState
            );
        }

        return result;
    }

    @PostMapping("/inject")
    public Map<String, Object> inject(
            @RequestBody MeshPacket packet) {

        meshSimulatorService.inject(packet);

        return Map.of(
                "status", "OK",
                "message", "Packet injected into mesh",
                "packetId", packet.getPacketId()
        );
    }

    @PostMapping("/gossip")
    public Map<String, Object> gossip() {

        int transfers =
                meshSimulatorService.gossip();

        return Map.of(
                "status", "OK",
                "message", "Gossip round completed",
                "transfers", transfers,
                "state",
                meshSimulatorService.getDevices()
        );
    }

    @PostMapping("/flush")
    public Map<String, Object> flush() {

        int processed = 0;
        int settled = 0;
        int duplicates = 0;
        int invalid = 0;

        for (MeshSimulatorService.BridgeUpload upload :
                meshSimulatorService.collectBridgeUploads()) {

            processed++;

            BridgeIngestionService.IngestionResult result =
                    bridgeIngestionService.ingest(
                            upload.packet()
                    );

            if ("SETTLED".equals(result.status())) {
                settled++;
            } else if ("DUPLICATE_DROPPED"
                    .equals(result.status())) {
                duplicates++;
            } else {
                invalid++;
            }
        }

        return Map.of(
                "status", "OK",
                "processed", processed,
                "settled", settled,
                "duplicates", duplicates,
                "invalid", invalid
        );
    }

    @PostMapping("/reset")
    public Map<String, String> reset() {

        meshSimulatorService.reset();

        return Map.of(
                "status", "OK",
                "message", "Mesh reset"
        );
    }
}
