package com.smarthome.implementor.nativeimpl;

import com.smarthome.domain.AlertMessage;
import com.smarthome.domain.DeliveryReceipt;
import com.smarthome.implementor.AlertChannel;
import com.smarthome.implementor.ChannelDeliveryException;

import java.time.Instant;
import java.util.UUID;

public class MobilePushChannel implements AlertChannel {

    @Override
    public DeliveryReceipt sendAlert(AlertMessage message) throws ChannelDeliveryException {
        if ("OFFLINE_PHONE".equalsIgnoreCase(message.locationZone())) {
            throw new ChannelDeliveryException(
                    ChannelDeliveryException.FailureReason.NETWORK_UNREACHABLE,
                    "Push notification server unreachable");
        }
        return new DeliveryReceipt(
                "PUSH-" + UUID.randomUUID().toString().substring(0, 8),
                getChannelName(),
                Instant.now());
    }

    @Override
    public boolean supportsRoute(String routeKey) {
        return "MOBILE_PUSH".equalsIgnoreCase(routeKey);
    }

    @Override
    public String getChannelName() {
        return "MobilePushNotificationService";
    }
}
