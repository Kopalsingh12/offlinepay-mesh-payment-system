package OfflinePay.wallet.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import OfflinePay.wallet.Wallet;
import OfflinePay.wallet.repository.WalletRepository;

class WalletServiceTest {

    private WalletRepository walletRepository;
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        walletService = new WalletService(walletRepository);
    }

    @Test
    void shouldTransferMoneyBetweenWallets() {

        Wallet sender = new Wallet();
        sender.setWalletId("WALLET001");
        sender.setBalance(1000);

        Wallet receiver = new Wallet();
        receiver.setWalletId("WALLET002");
        receiver.setBalance(500);

        when(walletRepository.findByWalletId("WALLET001"))
                .thenReturn(Optional.of(sender));

        when(walletRepository.findByWalletId("WALLET002"))
                .thenReturn(Optional.of(receiver));

        walletService.transfer(
                "WALLET001",
                "WALLET002",
                100
        );

        assertEquals(900, sender.getBalance());
        assertEquals(600, receiver.getBalance());

        verify(walletRepository).save(sender);
        verify(walletRepository).save(receiver);
    }

    @Test
    void shouldRejectInsufficientBalance() {

        Wallet sender = new Wallet();
        sender.setWalletId("WALLET001");
        sender.setBalance(50);

        Wallet receiver = new Wallet();
        receiver.setWalletId("WALLET002");
        receiver.setBalance(500);

        when(walletRepository.findByWalletId("WALLET001"))
                .thenReturn(Optional.of(sender));

        when(walletRepository.findByWalletId("WALLET002"))
                .thenReturn(Optional.of(receiver));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> walletService.transfer(
                                "WALLET001",
                                "WALLET002",
                                100
                        )
                );

        assertEquals(
                "Insufficient balance in sender wallet",
                exception.getMessage()
        );

        verify(walletRepository, never()).save(any());
    }

    @Test
    void shouldRejectNonPositiveAmount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> walletService.transfer(
                                "WALLET001",
                                "WALLET002",
                                0
                        )
                );

        assertEquals(
                "Transfer amount must be greater than zero",
                exception.getMessage()
        );

        verify(walletRepository, never())
                .findByWalletId(anyString());

        verify(walletRepository, never())
                .save(any());
    }
}