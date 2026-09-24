package OfflinePay.bridge;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import OfflinePay.transaction.dto.MeshPacket;

@RestController
@RequestMapping("/api/bridge")
public class BridgeController {

    private final BridgeIngestionService bridgeIngestionService;

    public BridgeController(
            BridgeIngestionService bridgeIngestionService) {

        this.bridgeIngestionService =
                bridgeIngestionService;
    }

    @PostMapping("/ingest")
    public BridgeIngestionService.IngestionResult ingest(
            @RequestBody MeshPacket packet) {

        return bridgeIngestionService.ingest(packet);
    }
}