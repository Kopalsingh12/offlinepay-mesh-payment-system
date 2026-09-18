package OfflinePay.transaction.dto;

public class MeshPacket {

    private String packetId;
    private String senderDeviceId;
    private String receiverDeviceId;
    private String payload;
    private int hopCount;

    public MeshPacket() {
    }

    public MeshPacket(
            String packetId,
            String senderDeviceId,
            String receiverDeviceId,
            String payload,
            int hopCount) {

        this.packetId = packetId;
        this.senderDeviceId = senderDeviceId;
        this.receiverDeviceId = receiverDeviceId;
        this.payload = payload;
        this.hopCount = hopCount;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public String getSenderDeviceId() {
        return senderDeviceId;
    }

    public void setSenderDeviceId(String senderDeviceId) {
        this.senderDeviceId = senderDeviceId;
    }

    public String getReceiverDeviceId() {
        return receiverDeviceId;
    }

    public void setReceiverDeviceId(String receiverDeviceId) {
        this.receiverDeviceId = receiverDeviceId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }
}
