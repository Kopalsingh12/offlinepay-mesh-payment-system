package OfflinePay.bridge;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import OfflinePay.crypto.CryptoService;
import OfflinePay.transaction.dto.MeshPacket;
import OfflinePay.transaction.dto.PaymentInstruction;

@Service
public class BridgeService {

    private final CryptoService cryptoService;

    private final Set<String> processedPacketIds =
            new HashSet<>();

    private final Set<String> processedNonces =
            new HashSet<>();

    private static final long MAX_PACKET_AGE_MILLIS =
            5 * 60 * 1000;

    public BridgeService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public synchronized String decryptPacket(
            MeshPacket packet) throws Exception {

        if (packet == null) {
            throw new IllegalArgumentException(
                    "Packet is required"
            );
        }

        if (packet.getPacketId() == null ||
                packet.getPacketId().isBlank()) {

            throw new IllegalArgumentException(
                    "Packet ID is required"
            );
        }

        if (processedPacketIds.contains(
                packet.getPacketId())) {

            throw new IllegalArgumentException(
                    "Replay detected: packet already processed"
            );
        }

        String decryptedPayload =
                cryptoService.decrypt(
                        packet.getPayload()
                );

        PaymentInstruction instruction =
                parsePaymentInstruction(
                        decryptedPayload
                );

        validatePayment(instruction);

        /*
         * Record packet only after:
         * 1. Successful decryption
         * 2. Successful parsing
         * 3. Successful validation
         */
        processedPacketIds.add(
                packet.getPacketId()
        );

        processedNonces.add(
                instruction.getNonce()
        );

        return decryptedPayload;
    }

    private PaymentInstruction parsePaymentInstruction(
            String payload) {

        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid payment payload"
            );
        }

        String[] parts =
                payload.split("\\|", -1);

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

    private void validatePayment(
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

        if (processedNonces.contains(
                instruction.getNonce())) {

            throw new IllegalArgumentException(
                    "Replay detected: payment nonce already processed"
            );
        }

        long currentTime =
                System.currentTimeMillis();

        long age =
                Math.abs(
                        currentTime -
                        instruction.getTimestamp()
                );

        if (age > MAX_PACKET_AGE_MILLIS) {

            throw new IllegalArgumentException(
                    "Payment packet has expired"
            );
        }
    }
}