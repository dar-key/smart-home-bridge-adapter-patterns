package com.smarthome.domain;

import java.util.Objects;

public record AlertMessage(
        String alertId,
        UrgencyLevel urgency,
        String title,
        String formattedBody,
        String locationZone) {
    public AlertMessage {
        Objects.requireNonNull(alertId, "alertId cannot be null");
        Objects.requireNonNull(urgency, "urgency cannot be null");
        Objects.requireNonNull(title, "title cannot be null");
        Objects.requireNonNull(formattedBody, "formattedBody cannot be null");
        Objects.requireNonNull(locationZone, "locationZone cannot be null");
    }
}
