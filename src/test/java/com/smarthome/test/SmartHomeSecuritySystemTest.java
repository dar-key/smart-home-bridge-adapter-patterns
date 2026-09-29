package com.smarthome.test;

import com.smarthome.abstraction.IntruderAlert;
import com.smarthome.abstraction.LowBatteryAlert;
import com.smarthome.domain.AlertMessage;
import com.smarthome.domain.DeliveryReceipt;
import com.smarthome.domain.IncidentData;
import com.smarthome.domain.UrgencyLevel;
import com.smarthome.implementor.AlertChannel;
import com.smarthome.implementor.ChannelDeliveryException;
import com.smarthome.implementor.adapter.RoofSirenChannelAdapter;
import com.smarthome.implementor.nativeimpl.MobilePushChannel;
import com.smarthome.implementor.nativeimpl.SmartWallHubChannel;
import com.smarthome.routing.ChannelResolver;
import com.smarthome.routing.DynamicChannelResolver;
import com.thirdparty.hardware.LegacyAnalogSirenHorn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmartHomeSecuritySystemTest {

        private AlertChannel mockPushChannel;
        private AlertChannel mockWallHubChannel;
        private ChannelResolver channelResolver;

        // Test stub representing the legacy hardware without requiring brittle bytecode
        // agents
        static class StubLegacySirenHorn extends LegacyAnalogSirenHorn {
                int forcedStatusCode = CODE_SUCCESS;
                boolean shouldThrowBurnout = false;
                int callCount = 0;

                @Override
                public int triggerSirenBuzzer(int decibelFrequency, int pulseCount, boolean strobeActivated) {
                        callCount++;
                        if (shouldThrowBurnout) {
                                throw new SirenHardwareBurnoutException("Oscillator coil burned out during test");
                        }
                        return forcedStatusCode;
                }
        }

        @BeforeEach
        void setUp() {
                mockPushChannel = mock(AlertChannel.class);
                mockWallHubChannel = mock(AlertChannel.class);

                when(mockPushChannel.supportsRoute("MOBILE_PUSH")).thenReturn(true);
                when(mockWallHubChannel.supportsRoute("WALL_HUB")).thenReturn(true);

                channelResolver = new DynamicChannelResolver(List.of(mockPushChannel, mockWallHubChannel));
        }

        @Test
        void testIntruderAlertDelegation() throws ChannelDeliveryException {
                DeliveryReceipt mockReceipt = new DeliveryReceipt("REC-001", "MockPush", Instant.now());
                when(mockPushChannel.sendAlert(any(AlertMessage.class))).thenReturn(mockReceipt);

                IntruderAlert intruderAlert = new IntruderAlert(channelResolver);
                IncidentData incident = new IncidentData(
                                "INC-1", "FrontDoorSensor", "LivingRoom", "Glass shattered", "MOBILE_PUSH");

                DeliveryReceipt receipt = intruderAlert.trigger(incident);

                assertNotNull(receipt);
                assertEquals("REC-001", receipt.confirmationId());

                ArgumentCaptor<AlertMessage> captor = ArgumentCaptor.forClass(AlertMessage.class);
                verify(mockPushChannel, times(1)).sendAlert(captor.capture());
                AlertMessage captured = captor.getValue();

                assertEquals(UrgencyLevel.CRITICAL, captured.urgency());
                assertTrue(captured.formattedBody().contains("*** INTRUDER BREACH ALERT ***"));
                assertTrue(captured.formattedBody().contains("FrontDoorSensor"));
        }

        @Test
        void testLowBatteryAlertDelegation() throws ChannelDeliveryException {
                DeliveryReceipt mockReceipt = new DeliveryReceipt("REC-002", "MockWall", Instant.now());
                when(mockWallHubChannel.sendAlert(any(AlertMessage.class))).thenReturn(mockReceipt);

                LowBatteryAlert batteryAlert = new LowBatteryAlert(channelResolver);
                IncidentData incident = new IncidentData(
                                "INC-2", "MotionSensorGarage", "Garage", "12% remaining", "WALL_HUB");

                DeliveryReceipt receipt = batteryAlert.trigger(incident);

                assertNotNull(receipt);
                assertEquals("REC-002", receipt.confirmationId());

                ArgumentCaptor<AlertMessage> captor = ArgumentCaptor.forClass(AlertMessage.class);
                verify(mockWallHubChannel, times(1)).sendAlert(captor.capture());
                AlertMessage captured = captor.getValue();

                assertEquals(UrgencyLevel.LOW, captured.urgency());
                assertTrue(captured.formattedBody().contains("Notice: Device 'MotionSensorGarage'"));
        }

        @Test
        void testAdapterSuccessfulTranslation() throws ChannelDeliveryException {
                StubLegacySirenHorn stubSiren = new StubLegacySirenHorn();
                stubSiren.forcedStatusCode = LegacyAnalogSirenHorn.CODE_SUCCESS;

                RoofSirenChannelAdapter adapter = new RoofSirenChannelAdapter(stubSiren);
                AlertMessage message = new AlertMessage(
                                "MSG-1", UrgencyLevel.CRITICAL, "Intruder Alert", "Test body", "Attic");

                DeliveryReceipt receipt = adapter.sendAlert(message);

                assertNotNull(receipt);
                assertTrue(receipt.confirmationId().startsWith("SIREN-"));
                assertEquals("LegacyAnalogRoofSiren-Adapter", receipt.channelName());
                assertEquals(1, stubSiren.callCount);
        }

        @Test
        void testAdapterLineDisconnectedTranslation() {
                StubLegacySirenHorn stubSiren = new StubLegacySirenHorn();
                stubSiren.forcedStatusCode = LegacyAnalogSirenHorn.ERR_LINE_DISCONNECTED;

                RoofSirenChannelAdapter adapter = new RoofSirenChannelAdapter(stubSiren);
                AlertMessage message = new AlertMessage(
                                "MSG-2", UrgencyLevel.CRITICAL, "Intruder", "Cut wire", "LivingRoom");

                ChannelDeliveryException ex = assertThrows(ChannelDeliveryException.class, () -> {
                        adapter.sendAlert(message);
                });

                assertEquals(ChannelDeliveryException.FailureReason.LINE_DISCONNECTED, ex.getReason());
                assertTrue(ex.getMessage().contains("-10"));
                assertEquals(1, stubSiren.callCount);
        }

        @Test
        void testAdapterHardwareBurnoutTranslation() {
                StubLegacySirenHorn stubSiren = new StubLegacySirenHorn();
                stubSiren.shouldThrowBurnout = true;

                RoofSirenChannelAdapter adapter = new RoofSirenChannelAdapter(stubSiren);
                AlertMessage message = new AlertMessage(
                                "MSG-3", UrgencyLevel.CRITICAL, "Intruder", "Extreme frequency", "Roof");

                ChannelDeliveryException ex = assertThrows(ChannelDeliveryException.class, () -> {
                        adapter.sendAlert(message);
                });

                assertEquals(ChannelDeliveryException.FailureReason.HARDWARE_FAULT, ex.getReason());
                assertTrue(ex.getMessage().contains("Oscillator coil burned out during test"));
                assertTrue(ex.getCause() instanceof LegacyAnalogSirenHorn.SirenHardwareBurnoutException);
                assertEquals(1, stubSiren.callCount);
        }

        @Test
        void testDynamicSelectionResolvesAdapterAtRuntime() throws ChannelDeliveryException {
                StubLegacySirenHorn stubSiren = new StubLegacySirenHorn();
                RoofSirenChannelAdapter sirenAdapter = new RoofSirenChannelAdapter(stubSiren);

                DynamicChannelResolver dynamicResolver = new DynamicChannelResolver(List.of(
                                new MobilePushChannel(),
                                new SmartWallHubChannel(),
                                sirenAdapter));

                IntruderAlert intruderAlert = new IntruderAlert(dynamicResolver);
                IncidentData incident = new IncidentData(
                                "INC-ROOF", "BackyardPIR", "Backyard", "Motion detected at 3 AM", "ROOF_SIREN");

                DeliveryReceipt receipt = intruderAlert.trigger(incident);

                assertNotNull(receipt);
                assertEquals("LegacyAnalogRoofSiren-Adapter", receipt.channelName());
                assertEquals(1, stubSiren.callCount);
        }
}
