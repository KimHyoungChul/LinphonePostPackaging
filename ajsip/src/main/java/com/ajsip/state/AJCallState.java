package com.ajsip.state;

public enum AJCallState {
    AjCallState_Idle(0),
    AjCallState_IncomingReceived(1),
    AjCallState_OutgoingInit(2),
    AjCallState_OutgoingProgress(3),
    AjCallState_OutgoingRinging(4),
    AjCallState_OutgoingEarlyMedia(5),
    AjCallState_Connected(6),
    AjCallState_StreamsRunning(7),
    AjCallState_Pausing(8),
    AjCallState_Paused(9),
    AjCallState_Resuming(10),
    AjCallState_Referred(11),
    AjCallState_Error(12),
    AjCallState_End(13),
    AjCallState_PausedByRemote(14),
    AjCallState_UpdatedByRemote(15),
    AjCallState_IncomingEarlyMedia(16),
    AjCallState_Updating(17),
    AjCallState_Released(18),
    AjCallState_EarlyUpdatedByRemote(19),
    AjCallState_EarlyUpdating(20);

    protected final int mValue;

    private AJCallState(int value) {
        this.mValue = value;
    }

    public static AJCallState fromInt(int value) throws RuntimeException {
        switch(value) {
            case 0:
                return AjCallState_Idle;
            case 1:
                return AjCallState_IncomingReceived;
            case 2:
                return AjCallState_OutgoingInit;
            case 3:
                return AjCallState_OutgoingProgress;
            case 4:
                return AjCallState_OutgoingRinging;
            case 5:
                return AjCallState_OutgoingEarlyMedia;
            case 6:
                return AjCallState_Connected;
            case 7:
                return AjCallState_StreamsRunning;
            case 8:
                return AjCallState_Pausing;
            case 9:
                return AjCallState_Paused;
            case 10:
                return AjCallState_Resuming;
            case 11:
                return AjCallState_Referred;
            case 12:
                return AjCallState_Error;
            case 13:
                return AjCallState_End;
            case 14:
                return AjCallState_PausedByRemote;
            case 15:
                return AjCallState_UpdatedByRemote;
            case 16:
                return AjCallState_IncomingEarlyMedia;
            case 17:
                return AjCallState_Updating;
            case 18:
                return AjCallState_Released;
            case 19:
                return AjCallState_EarlyUpdatedByRemote;
            case 20:
                return AjCallState_EarlyUpdating;
            default:
                throw new RuntimeException("Unhandled enum value " + value + " for State");
        }
    }

    public int toInt() {
        return this.mValue;
    }
}
