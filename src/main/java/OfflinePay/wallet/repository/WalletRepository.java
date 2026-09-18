package OfflinePay.wallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import OfflinePay.wallet.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByWalletId(String walletId);
}