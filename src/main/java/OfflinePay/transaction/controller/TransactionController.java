package OfflinePay.transaction.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import OfflinePay.transaction.entity.Transaction;
import OfflinePay.transaction.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public Transaction createTransaction(
            @RequestParam String senderWalletId,
            @RequestParam String receiverWalletId,
            @RequestParam double amount) {

        return transactionService.createTransaction(
                senderWalletId,
                receiverWalletId,
                amount
        );
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @PostMapping("/{transactionId}/settle")
    public Transaction settleTransaction(@PathVariable Long transactionId) {
        return transactionService.settleTransaction(transactionId);
    }
}