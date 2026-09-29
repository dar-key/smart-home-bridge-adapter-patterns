package com.smarthome.implementor;

public class ChannelDeliveryException extends Exception {

    public enum FailureReason {
        NETWORK_UNREACHABLE,
        DEVICE_OFFLINE,
        LINE_DISCONNECTED,
        HARDWARE_FAULT,
        UNAUTHORIZED
    }

    private final FailureReason reason;

    public ChannelDeliveryException(FailureReason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public ChannelDeliveryException(FailureReason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public FailureReason getReason() {
        return reason;
    }
}
