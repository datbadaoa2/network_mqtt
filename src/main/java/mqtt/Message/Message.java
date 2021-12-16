package mqtt.Message;
import java.io.Serializable;

public class Message implements Serializable {
    // Fixed header
    byte controlPacketType;
//    short payloadLength;
    // Payload
    String payload;

    public Message(ControlPacketType controlPacketType, String payload) {
        this.controlPacketType = (byte) controlPacketType.ordinal();
//        this.payloadLength = payloadLength;
        this.payload = payload;
    }

    public ControlPacketType getControlPacketType() {
        return ControlPacketType.values()[controlPacketType];
    }

    public void setControlPacketType(ControlPacketType controlPacketType) {
        this.controlPacketType = (byte)controlPacketType.ordinal();
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
