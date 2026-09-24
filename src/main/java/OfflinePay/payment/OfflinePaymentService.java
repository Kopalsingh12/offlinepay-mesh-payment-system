package OfflinePay.payment;

import java.util.UUID;

import org.springframework.stereotype.Service;

import OfflinePay.crypto.CryptoService;
import OfflinePay.crypto.ServerKeyHolder;
import OfflinePay.transaction.dto.MeshPacket;
import OfflinePay.transaction.dto.PaymentInstruction;

@Service
public class OfflinePaymentService {

    private final CryptoService cryptoService;
    private final ServerKeyHolder serverKeyHolder;

    private static final int DEFAULT_TTL = 5;

    public OfflinePaymentService(
            CryptoService cryptoService,
            ServerKeyHolder serverKeyHolder) {

        this.cryptoService = cryptoService;
        this.serverKeyHolder = serverKeyHolder;
    }

    public MeshPacket createOfflinePacket(
            PaymentInstruction instruction,
            String senderDeviceId,
            String receiverDeviceId) throws Exception {

        String payload =
                instruction.getSenderWalletId() + "|" +
                instruction.getReceiverWalletId() + "|" +
                instruction.getAmount() + "|" +
                instruction.getNonce() + "|" +
                instruction.getTimestamp();

        String encryptedPayload =
                cryptoService.encrypt(
                        payload,
                        serverKeyHolder.getPublicKey()
                );

        return new MeshPacket(
                UUID.randomUUID().toString(),
                senderDeviceId,
                receiverDeviceId,
                encryptedPayload,
                0,
                DEFAULT_TTL
        );
    }

    public String getServerPublicKey() {
        return serverKeyHolder.getPublicKeyBase64();
    }
}