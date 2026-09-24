package OfflinePay.wallet.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import OfflinePay.wallet.Wallet;
import OfflinePay.wallet.repository.WalletRepository;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet createWallet(String walletId, double balance) {

        if (walletId == null || walletId.isBlank()) {
            throw new IllegalArgumentException(
                    "Wallet ID must not be empty"
            );
        }

        if (balance < 0) {
            throw new IllegalArgumentException(
                    "Initial balance cannot be negative"
            );
        }

        if (walletRepository.findByWalletId(walletId).isPresent()) {
            throw new IllegalArgumentException(
                    "Wallet already exists: " + walletId
            );
        }

        Wallet wallet = new Wallet();
        wallet.setWalletId(walletId);
        wallet.setBalance(balance);

        return walletRepository.save(wallet);
    }

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    @Transactional
    public void transfer(
            String senderWalletId,
            String receiverWalletId,
            double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero"
            );
        }

        if (senderWalletId == null || receiverWalletId == null) {
            throw new IllegalArgumentException(
                    "Sender and receiver wallets are required"
            );
        }

        if (senderWalletId.equals(receiverWalletId)) {
            throw new IllegalArgumentException(
                    "Sender and receiver wallets must be different"
            );
        }

        Wallet sender = walletRepository
                .findByWalletId(senderWalletId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sender wallet not found: "
                                        + senderWalletId
                        ));

        Wallet receiver = walletRepository
                .findByWalletId(receiverWalletId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Receiver wallet not found: "
                                        + receiverWalletId
                        ));

        if (sender.getBalance() < amount) {
            throw new IllegalArgumentException(
                    "Insufficient balance in sender wallet"
            );
        }

        sender.setBalance(
                sender.getBalance() - amount
        );

        receiver.setBalance(
                receiver.getBalance() + amount
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);
    }
}