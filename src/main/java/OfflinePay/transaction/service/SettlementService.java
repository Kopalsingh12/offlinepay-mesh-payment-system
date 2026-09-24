package OfflinePay.transaction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import OfflinePay.transaction.dto.PaymentInstruction;
import OfflinePay.transaction.entity.Transaction;
import OfflinePay.transaction.repository.TransactionRepository;
import OfflinePay.wallet.Wallet;
import OfflinePay.wallet.repository.WalletRepository;

@Service
public class SettlementService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public SettlementService(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {

        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction settle(
            PaymentInstruction instruction,
            String packetHash,
            String bridgeNodeId,
            int hopCount) {

        if (instruction == null) {
            throw new IllegalArgumentException(
                    "Payment instruction is required"
            );
        }

        if (instruction.getAmount() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        Wallet sender = walletRepository
                .findByWalletId(
                        instruction.getSenderWalletId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown sender wallet: "
                                        + instruction.getSenderWalletId()
                        ));

        Wallet receiver = walletRepository
                .findByWalletId(
                        instruction.getReceiverWalletId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown receiver wallet: "
                                        + instruction.getReceiverWalletId()
                        ));

        if (sender.getBalance() < instruction.getAmount()) {

            return recordRejected(
                    instruction,
                    packetHash,
                    bridgeNodeId,
                    hopCount
            );
        }

        sender.setBalance(
                sender.getBalance()
                        - instruction.getAmount()
        );

        receiver.setBalance(
                receiver.getBalance()
                        + instruction.getAmount()
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        Transaction transaction = new Transaction();

        transaction.setTransactionId(
                instruction.getNonce()
        );

        transaction.setSenderWalletId(
                instruction.getSenderWalletId()
        );

        transaction.setReceiverWalletId(
                instruction.getReceiverWalletId()
        );

        transaction.setAmount(
                instruction.getAmount()
        );

        transaction.setStatus("SETTLED");

        return transactionRepository.save(transaction);
    }

    private Transaction recordRejected(
            PaymentInstruction instruction,
            String packetHash,
            String bridgeNodeId,
            int hopCount) {

        Transaction transaction = new Transaction();

        transaction.setTransactionId(
                instruction.getNonce()
        );

        transaction.setSenderWalletId(
                instruction.getSenderWalletId()
        );

        transaction.setReceiverWalletId(
                instruction.getReceiverWalletId()
        );

        transaction.setAmount(
                instruction.getAmount()
        );

        transaction.setStatus("REJECTED");

        return transactionRepository.save(transaction);
    }
}
