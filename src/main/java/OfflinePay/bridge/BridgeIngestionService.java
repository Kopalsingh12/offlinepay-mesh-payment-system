package OfflinePay.bridge;

import org.springframework.stereotype.Service;

import OfflinePay.crypto.CryptoService;
import OfflinePay.transaction.dto.MeshPacket;
import OfflinePay.transaction.dto.PaymentInstruction;
import OfflinePay.transaction.entity.Transaction;
import OfflinePay.transaction.service.SettlementService;

@Service
public class BridgeIngestionService {

    private final CryptoService cryptoService;
    private final IdempotencyService idempotencyService;
    private final SettlementService settlementService;

    private static final long MAX_PACKET_AGE_MILLIS =
            5 * 60 * 1000;

    public BridgeIngestionService(
            CryptoService cryptoService,
            IdempotencyService idempotencyService,
            SettlementService settlementService) {

        this.cryptoService = cryptoService;
        this.idempotencyService = idempotencyService;
        this.settlementService = settlementService;
    }

    public IngestionResult ingest(MeshPacket packet) {

        if (packet == null) {
            return IngestionResult.invalid(
                    "Packet is required"
            );
        }

        if (packet.getPayload() == null ||
                packet.getPayload().isBlank()) {

            return IngestionResult.invalid(
                    "Packet payload is required"
            );
        }

        try {

            String packetHash =
                    cryptoService.hashCiphertext(
                            packet.getPayload()
                    );

            if (!idempotencyService.claim(packetHash)) {
                return IngestionResult.duplicate();
            }

            String decryptedPayload =
                    cryptoService.decrypt(
                            packet.getPayload()
                    );

            PaymentInstruction instruction =
                    parsePaymentInstruction(
                            decryptedPayload
                    );

            validateInstruction(instruction);

            Transaction transaction =
                    settlementService.settle(
                            instruction,
                            packetHash,
                            packet.getReceiverDeviceId(),
                            packet.getHopCount()
                    );

            if ("REJECTED".equals(transaction.getStatus())) {

                return IngestionResult.invalid(
                        "Insufficient balance in sender wallet"
                );
            }

            return IngestionResult.settled();

        } catch (Exception ex) {

            return IngestionResult.invalid(
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Invalid payment packet"
            );
        }
    }

    private PaymentInstruction parsePaymentInstruction(
            String payload) {

        String[] parts =
                payload.split("\\|");

        if (parts.length != 5) {
            throw new IllegalArgumentException(
                    "Invalid payment payload"
            );
        }

        try {

            return new PaymentInstruction(
                    parts[0],
                    parts[1],
                    Double.parseDouble(parts[2]),
                    parts[3],
                    Long.parseLong(parts[4])
            );

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    "Invalid payment data"
            );
        }
    }

    private void validateInstruction(
            PaymentInstruction instruction) {

        if (instruction.getSenderWalletId() == null ||
                instruction.getSenderWalletId().isBlank()) {

            throw new IllegalArgumentException(
                    "Sender wallet ID is required"
            );
        }

        if (instruction.getReceiverWalletId() == null ||
                instruction.getReceiverWalletId().isBlank()) {

            throw new IllegalArgumentException(
                    "Receiver wallet ID is required"
            );
        }

        if (instruction.getSenderWalletId()
                .equals(instruction.getReceiverWalletId())) {

            throw new IllegalArgumentException(
                    "Sender and receiver wallets must be different"
            );
        }

        if (instruction.getAmount() <= 0) {

            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (instruction.getNonce() == null ||
                instruction.getNonce().isBlank()) {

            throw new IllegalArgumentException(
                    "Payment nonce is required"
            );
        }

        long age =
                Math.abs(
                        System.currentTimeMillis()
                                - instruction.getTimestamp()
                );

        if (age > MAX_PACKET_AGE_MILLIS) {

            throw new IllegalArgumentException(
                    "Payment packet has expired"
            );
        }
    }

    public record IngestionResult(
            String status,
            String message) {

        public static IngestionResult settled() {
            return new IngestionResult(
                    "SETTLED",
                    "Payment settled successfully"
            );
        }

        public static IngestionResult duplicate() {
            return new IngestionResult(
                    "DUPLICATE_DROPPED",
                    "Payment packet already processed"
            );
        }

        public static IngestionResult invalid(
                String message) {

            return new IngestionResult(
                    "INVALID",
                    message
            );
        }
    }
}