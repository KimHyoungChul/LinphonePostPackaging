package com.ajsip.state;

public enum AJGlobalState {
    AJGlobal_Off(0),
    AJGlobal_Startup(1),
    AJGlobal_On(2),
    AJGlobal_Shutdown(3),
    AJGlobal_Configuring(4);

    protected final int mValue;

    private AJGlobalState(int value) {
        this.mValue = value;
    }

    public static AJGlobalState fromInt(int value) throws RuntimeException {
        switch (value) {
            case 0:
                return AJGlobal_Off;
            case 1:
                return AJGlobal_Startup;
            case 2:
                return AJGlobal_On;
            case 3:
                return AJGlobal_Shutdown;
            case 4:
                return AJGlobal_Configuring;
            default:
                throw new RuntimeException("Unhandled enum value " + value + " for GlobalState");
        }
    }

    public int toInt() {
        return this.mValue;
    }
}
