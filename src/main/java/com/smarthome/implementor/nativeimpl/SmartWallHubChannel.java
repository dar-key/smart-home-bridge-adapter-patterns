package com.smarthome.implementor.nativeimpl;

import com.smarthome.domain.AlertMessage;
import com.smarthome.domain.DeliveryReceipt;
import com.smarthome.implementor.AlertChannel;
import com.smarthome.implementor.ChannelDeliveryException;

import java.time.Instant;
import java.util.UUID;

public class SmartWallHubChannel implements AlertChannel {

    @Override
    public DeliveryReceipt sendAlert(AlertMessage message) throws ChannelDeliveryException {
        if ("BASEMENT_HUB".equalsIgnoreCase(message.locationZone())) {
            throw new ChannelDeliveryException(
                    ChannelDeliveryException.FailureReason.DEVICE_OFFLINE,
                    "Wall panel display is unresponsive");
        }
        return new DeliveryReceipt(
                "WALL-" + UUID.randomUUID().toString().substring(0, 8),
                getChannelName(),
                Instant.now());
    }

    @Override
    public boolean supportsRoute(String routeKey) {
        return "WALL_HUB".equalsIgnoreCase(routeKey);
    }

    @Override
    public String getChannelName() {
        return "SmartWallTouchscreenHub";
    }
}
