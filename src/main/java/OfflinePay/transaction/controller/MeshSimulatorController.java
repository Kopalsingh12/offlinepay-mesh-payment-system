package OfflinePay.transaction.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import OfflinePay.transaction.service.MeshSimulatorService;
import OfflinePay.transaction.service.VirtualDevice;

@RestController
@RequestMapping("/api/mesh")
public class MeshSimulatorController {

    private final MeshSimulatorService meshSimulatorService;

    public MeshSimulatorController(
            MeshSimulatorService meshSimulatorService) {
        this.meshSimulatorService = meshSimulatorService;
    }

    @GetMapping("/state")
    public Map<String, Object> state() {

        Map<String, Object> result =
                new LinkedHashMap<>();

        for (Map.Entry<String, VirtualDevice> entry :
                meshSimulatorService.getDevices().entrySet()) {

            VirtualDevice device = entry.getValue();

            Map<String, Object> deviceState =
                    new LinkedHashMap<>();

            deviceState.put("deviceId",
                    device.getDeviceId());

            deviceState.put("hasInternet",
                    device.hasInternet());

            deviceState.put("packetCount",
                    device.packetCount());

            result.put(entry.getKey(), deviceState);
        }

        return result;
    }

    @PostMapping("/gossip")
    public Map<String, String> gossip() {

        meshSimulatorService.gossip();

        return Map.of(
                "status", "OK",
                "message", "Gossip round completed"
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
