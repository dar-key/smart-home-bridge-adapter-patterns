package com.smarthome.routing;

import com.smarthome.implementor.AlertChannel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DynamicChannelResolver implements ChannelResolver {

    private final List<AlertChannel> registeredChannels = new CopyOnWriteArrayList<>();

    public DynamicChannelResolver(List<AlertChannel> initialChannels) {
        if (initialChannels != null) {
            this.registeredChannels.addAll(initialChannels);
        }
    }

    public void registerChannel(AlertChannel channel) {
        registeredChannels.add(channel);
    }

    @Override
    public AlertChannel resolve(String routeKey) {
        return registeredChannels.stream()
                .filter(channel -> channel.supportsRoute(routeKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No channel registered for route: " + routeKey));
    }
}
