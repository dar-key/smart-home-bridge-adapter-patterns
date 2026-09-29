package com.smarthome.implementor;

import com.smarthome.domain.AlertMessage;
import com.smarthome.domain.DeliveryReceipt;

public interface AlertChannel {
    DeliveryReceipt sendAlert(AlertMessage message) throws ChannelDeliveryException;

    boolean supportsRoute(String routeKey);

    String getChannelName();
}
