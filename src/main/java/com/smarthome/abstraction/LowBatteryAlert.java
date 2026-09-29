package com.smarthome.abstraction;

import com.smarthome.domain.AlertMessage;
import com.smarthome.domain.DeliveryReceipt;
import com.smarthome.domain.IncidentData;
import com.smarthome.domain.UrgencyLevel;
import com.smarthome.implementor.AlertChannel;
import com.smarthome.implementor.ChannelDeliveryException;
import com.smarthome.routing.ChannelResolver;

public class LowBatteryAlert extends SecurityAlert {

    public LowBatteryAlert(ChannelResolver channelResolver) {
        super(channelResolver);
    }

    @Override
    protected DeliveryReceipt buildAndSend(
            IncidentData incident,
            AlertChannel channel) throws ChannelDeliveryException {
        String formatted = String.format(
                "Notice: Device '%s' in %s is low on battery (%s). Please recharge.",
                incident.sensorName(),
                incident.roomLocation(),
                incident.rawDetails());

        AlertMessage message = new AlertMessage(
                incident.incidentId(),
                UrgencyLevel.LOW,
                "Battery Maintenance",
                formatted,
                incident.roomLocation());

        return channel.sendAlert(message);
    }
}
