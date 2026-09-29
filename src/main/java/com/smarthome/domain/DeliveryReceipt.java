package com.smarthome.domain;

import java.time.Instant;

public record DeliveryReceipt(
        String confirmationId,
        String channelName,
        Instant timestamp) {
}
