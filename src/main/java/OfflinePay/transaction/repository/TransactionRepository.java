package OfflinePay.transaction.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import OfflinePay.transaction.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);
}
