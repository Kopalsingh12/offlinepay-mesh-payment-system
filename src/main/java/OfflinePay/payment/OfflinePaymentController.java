package OfflinePay.payment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import OfflinePay.transaction.dto.MeshPacket;
import OfflinePay.transaction.dto.PaymentInstruction;

@RestController
@RequestMapping("/api/offline-payments")
public class OfflinePaymentController {

    private final OfflinePaymentService offlinePaymentService;

    public OfflinePaymentController(
            OfflinePaymentService offlinePaymentService) {
        this.offlinePaymentService = offlinePaymentService;
    }

    @GetMapping("/key")
    public String generateKey() throws Exception {
        return offlinePaymentService.generateSecretKey();
    }

    @PostMapping("/create")
    public MeshPacket createOfflinePayment(
            @RequestBody PaymentInstruction instruction,
            @RequestParam String secretKey,
            @RequestParam String senderDeviceId,
            @RequestParam String receiverDeviceId) throws Exception {

        return offlinePaymentService.createOfflinePacket(
                instruction,
                secretKey,
                senderDeviceId,
                receiverDeviceId
        );
    }
}