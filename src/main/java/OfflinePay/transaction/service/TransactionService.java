package OfflinePay.transaction.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import OfflinePay.transaction.entity.Transaction;
import OfflinePay.transaction.repository.TransactionRepository;
import OfflinePay.wallet.service.WalletService;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    public TransactionService(
            TransactionRepository transactionRepository,
            WalletService walletService) {

        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
    }

    public Transaction createTransaction(
            String senderWalletId,
            String receiverWalletId,
            double amount) {

        if (senderWalletId == null || senderWalletId.isBlank()) {
            throw new IllegalArgumentException(
                    "Sender wallet ID is required"
            );
        }

        if (receiverWalletId == null || receiverWalletId.isBlank()) {
            throw new IllegalArgumentException(
                    "Receiver wallet ID is required"
            );
        }

        if (senderWalletId.equals(receiverWalletId)) {
            throw new IllegalArgumentException(
                    "Sender and receiver wallets must be different"
            );
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero"
            );
        }

        Transaction transaction = new Transaction();

        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setSenderWalletId(senderWalletId);
        transaction.setReceiverWalletId(receiverWalletId);
        transaction.setAmount(amount);
        transaction.setStatus("PENDING");

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Transactional
    public Transaction settleTransaction(Long transactionId) {

        Transaction transaction = transactionRepository
                .findById(transactionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Transaction not found: " + transactionId
                        ));

        if ("SETTLED".equals(transaction.getStatus())) {
            return transaction;
        }

        if (!"PENDING".equals(transaction.getStatus())) {
            throw new IllegalStateException(
                    "Transaction cannot be settled. Current status: "
                            + transaction.getStatus()
            );
        }

        walletService.transfer(
                transaction.getSenderWalletId(),
                transaction.getReceiverWalletId(),
                transaction.getAmount()
        );

        transaction.setStatus("SETTLED");

        return transactionRepository.save(transaction);
    }
}