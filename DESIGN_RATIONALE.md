# Design Rationale: Smart Home Security System

### 1. Problem Domain

A home security system that separates the types of alerts from the devices that send them:

- **Alerts:** `IntruderAlert` (high urgency), `LowBatteryAlert` (low urgency reminder), and `FireHazardAlert`.
- **Devices:** `MobilePushChannel` (phone app), `SmartWallHubChannel` (wall screen), and `CloudAuditLogChannel`.

### 2. Why Bridge Alone Is Not Enough

Bridge needs every device to implement our `AlertChannel` interface. It cannot use our old roof siren (`LegacyAnalogSirenHorn`) because that is an outside library with completely different methods, and we cannot edit its code.

### 3. Why Adapter Alone Is Not Enough

Adapter only fixes the connection to that one old siren. If we only used Adapter without Bridge, we would have to create separate classes for every combination (`IntruderOnPhone`, `IntruderOnSiren`, `BatteryOnPhone`, `BatteryOnSiren`). Bridge is needed to keep alerts and devices separate so we do not get class explosion.

### 4. Why the Old Siren Is Incompatible

The `LegacyAnalogSirenHorn` class does not fit our system in three ways:

1. **Wrong method name:** It uses `triggerSirenBuzzer` instead of `sendAlert`.
2. **Wrong parameters:** It asks for raw numbers and a boolean `(int decibels, int pulses, boolean strobe)` instead of our `AlertMessage` object.
3. **Wrong error handling:** It returns error numbers (`-10`, `-20`) and throws an unchecked `SirenHardwareBurnoutException` instead of our `ChannelDeliveryException`.

The `RoofSirenChannelAdapter` fixes this. It translates the data, catches the foreign crash, checks the error numbers, and throws our clean `ChannelDeliveryException`. No outside siren code leaks into the rest of the project.

### 5. Complexity Module: Dynamic implementor selection

The user code never hard-codes which device to use. When an alert runs, it reads `preferredRouteKey` from the input data. The `DynamicChannelResolver` looks at this key at runtime and automatically picks the right channel, including the adapted siren.

### 6. Open/Closed Principle

- **Adding an alert:** I added `FireHazardAlert` by extending `SecurityAlert`. No existing channels or alerts were changed.
- **Adding a device:** I added `CloudAuditLogChannel` by implementing `AlertChannel`. No abstractions or existing channels were changed.

### 7. Design limitation

Our `AlertChannel` interface is built to send text messages. Because of this high-level design, you cannot adjust low-level hardware settings on the siren (like changing its sound pitch frequency) directly from the alert code.
