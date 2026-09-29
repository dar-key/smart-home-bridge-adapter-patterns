package com.smarthome.domain;

public record IncidentData(
        String incidentId,
        String sensorName,
        String roomLocation,
        String rawDetails,
        String preferredRouteKey) {
}
