package com.smarthome.implementor.nativeimpl;

import com.smarthome.domain.AlertMessage;
import com.smarthome.domain.DeliveryReceipt;
import com.smarthome.implementor.AlertChannel;
import com.smarthome.implementor.ChannelDeliveryException;

import java.time.Instant;
import java.util.UUID;

public class CloudAuditLogChannel implements AlertChannel {

    @Override
    public DeliveryReceipt sendAlert(AlertMessage message) throws ChannelDeliveryException {
        return new DeliveryReceipt(
                "CLOUD-" + UUID.randomUUID().toString().substring(0, 8),
                getChannelName(),
                Instant.now());
    }

    @Override
    public boolean supportsRoute(String routeKey) {
        return "CLOUD_AUDIT".equalsIgnoreCase(routeKey);
    }

    @Override
    public String getChannelName() {
        return "CloudComplianceAuditLogger";
    }
}
