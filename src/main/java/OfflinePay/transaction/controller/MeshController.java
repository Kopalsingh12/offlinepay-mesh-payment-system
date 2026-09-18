package OfflinePay.transaction.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import OfflinePay.transaction.dto.MeshPacket;
import OfflinePay.transaction.service.MeshService;

@RestController
@RequestMapping("/api/mesh")
public class MeshController {

    private final MeshService meshService;

    public MeshController(MeshService meshService) {
        this.meshService = meshService;
    }

    @PostMapping("/receive")
    public MeshPacket receivePacket(@RequestBody MeshPacket packet) {
        return meshService.receivePacket(packet);
    }

    @GetMapping("/packets")
    public List<MeshPacket> getPackets() {
        return meshService.getPackets();
    }

    @DeleteMapping("/clear")
    public String clearPackets() {
        meshService.clearPackets();
        return "Mesh packets cleared";
    }
}
