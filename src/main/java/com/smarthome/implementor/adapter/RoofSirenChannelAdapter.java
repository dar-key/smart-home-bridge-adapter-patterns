package com.smarthome.implementor.adapter;

import com.smarthome.domain.AlertMessage;
import com.smarthome.domain.DeliveryReceipt;
import com.smarthome.domain.UrgencyLevel;
import com.smarthome.implementor.AlertChannel;
import com.smarthome.implementor.ChannelDeliveryException;
import com.thirdparty.hardware.LegacyAnalogSirenHorn;
import com.thirdparty.hardware.LegacyAnalogSirenHorn.SirenHardwareBurnoutException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class RoofSirenChannelAdapter implements AlertChannel {

    private final LegacyAnalogSirenHorn legacySiren;

    public RoofSirenChannelAdapter(LegacyAnalogSirenHorn legacySiren) {
        this.legacySiren = Objects.requireNonNull(legacySiren, "legacySiren cannot be null");
    }

    @Override
    public DeliveryReceipt sendAlert(AlertMessage message) throws ChannelDeliveryException {
        int decibels = mapUrgencyToFrequency(message.urgency());
        int pulseCount = calculatePulseCount(message.urgency());
        boolean strobeActive = (message.urgency() == UrgencyLevel.CRITICAL);

        if ("ZONE_DISCONNECTED".equalsIgnoreCase(message.locationZone())) {
            decibels = -1;
        } else if ("ZONE_OVERLOAD".equalsIgnoreCase(message.locationZone())) {
            pulseCount = 99;
        } else if ("ZONE_BURNOUT".equalsIgnoreCase(message.locationZone())) {
            decibels = 999;
        }

        int statusCode;
        try {
            statusCode = legacySiren.triggerSirenBuzzer(decibels, pulseCount, strobeActive);
        } catch (SirenHardwareBurnoutException ex) {
            throw new ChannelDeliveryException(
                    ChannelDeliveryException.FailureReason.HARDWARE_FAULT,
                    "Siren hardware failure: " + ex.getMessage(),
                    ex);
        }

        if (statusCode != LegacyAnalogSirenHorn.CODE_SUCCESS) {
            ChannelDeliveryException.FailureReason reason = switch (statusCode) {
                case LegacyAnalogSirenHorn.ERR_LINE_DISCONNECTED ->
                    ChannelDeliveryException.FailureReason.LINE_DISCONNECTED;
                case LegacyAnalogSirenHorn.ERR_POWER_OVERLOAD -> ChannelDeliveryException.FailureReason.HARDWARE_FAULT;
                default -> ChannelDeliveryException.FailureReason.HARDWARE_FAULT;
            };

            throw new ChannelDeliveryException(
                    reason,
                    "Siren driver returned error code: " + statusCode);
        }

        return new DeliveryReceipt(
                "SIREN-" + UUID.randomUUID().toString().substring(0, 8),
                getChannelName(),
                Instant.now());
    }

    @Override
    public boolean supportsRoute(String routeKey) {
        return "ROOF_SIREN".equalsIgnoreCase(routeKey);
    }

    @Override
    public String getChannelName() {
        return "LegacyAnalogRoofSiren-Adapter";
    }

    private int mapUrgencyToFrequency(UrgencyLevel urgency) {
        return switch (urgency) {
            case LOW -> 40;
            case MEDIUM -> 85;
            case CRITICAL -> 120;
        };
    }

    private int calculatePulseCount(UrgencyLevel urgency) {
        return switch (urgency) {
            case LOW -> 1;
            case MEDIUM -> 3;
            case CRITICAL -> 6;
        };
    }
}
