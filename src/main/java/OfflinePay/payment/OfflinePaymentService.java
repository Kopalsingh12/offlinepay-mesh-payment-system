package OfflinePay.payment;

import java.util.UUID;

import org.springframework.stereotype.Service;

import OfflinePay.crypto.CryptoService;
import OfflinePay.transaction.dto.MeshPacket;
import OfflinePay.transaction.dto.PaymentInstruction;

@Service
public class OfflinePaymentService {

    private final CryptoService cryptoService;

    public OfflinePaymentService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public MeshPacket createOfflinePacket(
            PaymentInstruction instruction,
            String secretKey,
            String senderDeviceId,
            String receiverDeviceId) throws Exception {

        String payload =
                instruction.getSenderWalletId() + "|" +
                instruction.getReceiverWalletId() + "|" +
                instruction.getAmount() + "|" +
                instruction.getNonce() + "|" +
                instruction.getTimestamp();

        String encryptedPayload =
                cryptoService.encrypt(payload, secretKey);

        return new MeshPacket(
                UUID.randomUUID().toString(),
                senderDeviceId,
                receiverDeviceId,
                encryptedPayload,
                0
        );
    }

    public String generateSecretKey() throws Exception {
        return cryptoService.generateSecretKey();
    }
}