package OfflinePay.wallet.service;

import java.util.List;

import org.springframework.stereotype.Service;

import OfflinePay.wallet.Wallet;
import OfflinePay.wallet.repository.WalletRepository;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet createWallet(String walletId, double balance) {

        Wallet wallet = new Wallet();

        wallet.setWalletId(walletId);
        wallet.setBalance(balance);

        return walletRepository.save(wallet);
    }

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }
}