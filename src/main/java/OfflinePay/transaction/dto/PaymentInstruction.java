package OfflinePay.transaction.dto;

public class PaymentInstruction {

    private String senderWalletId;
    private String receiverWalletId;
    private double amount;
    private String nonce;
    private long timestamp;

    public PaymentInstruction() {
    }

    public PaymentInstruction(
            String senderWalletId,
            String receiverWalletId,
            double amount,
            String nonce,
            long timestamp) {

        this.senderWalletId = senderWalletId;
        this.receiverWalletId = receiverWalletId;
        this.amount = amount;
        this.nonce = nonce;
        this.timestamp = timestamp;
    }

    public String getSenderWalletId() {
        return senderWalletId;
    }

    public void setSenderWalletId(String senderWalletId) {
        this.senderWalletId = senderWalletId;
    }

    public String getReceiverWalletId() {
        return receiverWalletId;
    }

    public void setReceiverWalletId(String receiverWalletId) {
        this.receiverWalletId = receiverWalletId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
