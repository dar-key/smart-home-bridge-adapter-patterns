package com.smarthome.abstraction;

import com.smarthome.domain.DeliveryReceipt;
import com.smarthome.domain.IncidentData;
import com.smarthome.implementor.AlertChannel;
import com.smarthome.implementor.ChannelDeliveryException;
import com.smarthome.routing.ChannelResolver;

import java.util.Objects;

public abstract class SecurityAlert {

    protected final ChannelResolver channelResolver;

    protected SecurityAlert(ChannelResolver channelResolver) {
        this.channelResolver = Objects.requireNonNull(channelResolver, "channelResolver cannot be null");
    }

    public DeliveryReceipt trigger(IncidentData incident) throws ChannelDeliveryException {
        AlertChannel channel = channelResolver.resolve(incident.preferredRouteKey());
        return buildAndSend(incident, channel);
    }

    protected abstract DeliveryReceipt buildAndSend(
        IncidentData incident,
        AlertChannel channel
    ) throws ChannelDeliveryException;
}
