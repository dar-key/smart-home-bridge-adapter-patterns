package com.smarthome.routing;

import com.smarthome.implementor.AlertChannel;

public interface ChannelResolver {
    AlertChannel resolve(String routeKey);
}
