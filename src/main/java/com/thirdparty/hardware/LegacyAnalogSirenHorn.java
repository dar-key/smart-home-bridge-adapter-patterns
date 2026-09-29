package com.thirdparty.hardware;

public class LegacyAnalogSirenHorn {

    public static final int CODE_SUCCESS = 0;
    public static final int ERR_LINE_DISCONNECTED = -10;
    public static final int ERR_POWER_OVERLOAD = -20;

    public static class SirenHardwareBurnoutException extends RuntimeException {
        public SirenHardwareBurnoutException(String message) {
            super(message);
        }
    }

    public int triggerSirenBuzzer(int decibelFrequency, int pulseCount, boolean strobeActivated)
            throws SirenHardwareBurnoutException {
        if (decibelFrequency > 140) {
            throw new SirenHardwareBurnoutException("Oscillator coil overload at " + decibelFrequency + "Hz");
        }
        if (decibelFrequency <= 0) {
            return ERR_LINE_DISCONNECTED;
        }
        if (pulseCount > 10 && strobeActivated) {
            return ERR_POWER_OVERLOAD;
        }
        return CODE_SUCCESS;
    }
}
