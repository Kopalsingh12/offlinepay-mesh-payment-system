package OfflinePay.transaction.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import OfflinePay.transaction.entity.Transaction;
import OfflinePay.transaction.repository.TransactionRepository;
import OfflinePay.wallet.Wallet;
import OfflinePay.wallet.repository.WalletRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            WalletRepository walletRepository) {

        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    public Transaction createTransaction(
            String senderWalletId,
            String receiverWalletId,
            double amount) {

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
    public Transaction processTransaction(String transactionId) {

        Transaction transaction = transactionRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() ->
                        new RuntimeException("Transaction not found"));

        if (!transaction.getStatus().equals("PENDING")) {
            throw new RuntimeException("Transaction already processed");
        }

        Wallet sender = walletRepository
                .findByWalletId(transaction.getSenderWalletId())
                .orElseThrow(() ->
                        new RuntimeException("Sender wallet not found"));

        Wallet receiver = walletRepository
                .findByWalletId(transaction.getReceiverWalletId())
                .orElseThrow(() ->
                        new RuntimeException("Receiver wallet not found"));

        if (sender.getBalance() < transaction.getAmount()) {
            transaction.setStatus("FAILED");
            return transactionRepository.save(transaction);
        }

        sender.setBalance(
                sender.getBalance() - transaction.getAmount()
        );

        receiver.setBalance(
                receiver.getBalance() + transaction.getAmount()
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        transaction.setStatus("SUCCESS");

        return transactionRepository.save(transaction);
    }
}