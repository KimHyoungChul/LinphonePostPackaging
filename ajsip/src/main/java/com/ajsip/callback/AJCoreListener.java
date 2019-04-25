package com.ajsip.callback;

import com.ajsip.state.AJGlobalState;
import com.ajsip.state.AJRegistrationState;
import com.ajsip.state.AJCallState;

public interface AJCoreListener {
    public void onCallStateChanged(AJCallState state, String message);
    public void onRegistrationStateChanged(AJRegistrationState state, String message);
    public void onGlobalStateChanged(AJGlobalState gstate, String message);
}
