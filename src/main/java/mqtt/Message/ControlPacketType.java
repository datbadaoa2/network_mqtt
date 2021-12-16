package mqtt.Message;

public enum ControlPacketType {
    RESERVED,
    CONNECT,
    CONNACK,
    PUBLISH,
    PUBACK,
    PUBREC,
    PUBREL,
    PUBCOMP,
    SUBSCRIBE,
    SUBACK,
    UNSUB,
    UNSUBACK,
    PING,
    PINGACK,
    DISCONNECT,
}
