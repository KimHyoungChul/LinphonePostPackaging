package com.ajsip.callback;

import com.ajsip.state.AJRegistrationState;
import com.ajsip.state.AJCallState;
import com.ajsip.state.AJGlobalState;

import org.linphone.core.AuthInfo;
import org.linphone.core.AuthMethod;
import org.linphone.core.Call;
import org.linphone.core.CallLog;
import org.linphone.core.CallStats;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.ConfiguringState;
import org.linphone.core.Content;
import org.linphone.core.Core;
import org.linphone.core.CoreListener;
import org.linphone.core.EcCalibratorStatus;
import org.linphone.core.Event;
import org.linphone.core.Friend;
import org.linphone.core.FriendList;
import org.linphone.core.GlobalState;
import org.linphone.core.InfoMessage;
import org.linphone.core.PresenceModel;
import org.linphone.core.ProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.RegistrationState;
import org.linphone.core.SubscriptionState;
import org.linphone.core.VersionUpdateCheckResult;

public class MyCoreListenerStub implements CoreListener {
    AJCoreListenerStub ajCoreListenerStub;

    public MyCoreListenerStub(AJCoreListenerStub ajCoreListenerStub) {
        this.ajCoreListenerStub = ajCoreListenerStub;
    }

    @Override
    public void onGlobalStateChanged(Core core, GlobalState globalState, String s) {
        AJGlobalState ajGlobalState = AJGlobalState.AJGlobal_Off;
        switch (globalState) {
            case On:
                ajGlobalState = AJGlobalState.AJGlobal_On;
                break;
            case Off:
                ajGlobalState = AJGlobalState.AJGlobal_Off;
                break;
            case Startup:
                ajGlobalState = AJGlobalState.AJGlobal_Startup;
                break;
            case Shutdown:
                ajGlobalState = AJGlobalState.AJGlobal_Shutdown;
                break;
            case Configuring:
                ajGlobalState = AJGlobalState.AJGlobal_Configuring;
                break;

        }
        try {
            ajCoreListenerStub.onGlobalStateChanged(ajGlobalState, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRegistrationStateChanged(Core core, ProxyConfig proxyConfig, RegistrationState registrationState, String s) {
        AJRegistrationState ajRegistrationState = AJRegistrationState.AJRegistration_None;
        switch (registrationState) {
            case None:
                ajRegistrationState = AJRegistrationState.AJRegistration_None;
                break;
            case Progress:
                ajRegistrationState = AJRegistrationState.AJRegistration_Progress;
                break;
            case Ok:
                ajRegistrationState = AJRegistrationState.AJRegistration_Ok;
                break;
            case Cleared:
                ajRegistrationState = AJRegistrationState.AJRegistration_Cleared;
                break;
            case Failed:
                ajRegistrationState = AJRegistrationState.AJRegistration_Failed;
                break;
        }
        try {
            ajCoreListenerStub.onRegistrationStateChanged(ajRegistrationState, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCallStateChanged(Core core, Call call, Call.State state, String s) {
        AJCallState ajCallState = AJCallState.AjCallState_Released;
        switch (state) {
            case Idle:
                ajCallState = AJCallState.AjCallState_Idle;
                break;
            case IncomingReceived:
                ajCallState = AJCallState.AjCallState_IncomingReceived;
                break;
            case OutgoingInit:
                ajCallState = AJCallState.AjCallState_OutgoingInit;
                break;
            case OutgoingProgress:
                ajCallState = AJCallState.AjCallState_OutgoingProgress;
                break;
            case OutgoingRinging:
                ajCallState = AJCallState.AjCallState_OutgoingRinging;
                break;
            case OutgoingEarlyMedia:
                ajCallState = AJCallState.AjCallState_OutgoingEarlyMedia;
                break;
            case Connected:
                ajCallState = AJCallState.AjCallState_Connected;
                break;
            case StreamsRunning:
                ajCallState = AJCallState.AjCallState_StreamsRunning;
                break;
            case Pausing:
                ajCallState = AJCallState.AjCallState_Pausing;
                break;
            case Paused:
                ajCallState = AJCallState.AjCallState_Paused;
                break;
            case Resuming:
                ajCallState = AJCallState.AjCallState_Resuming;
                break;
            case Referred:
                ajCallState = AJCallState.AjCallState_Referred;
                break;
            case Error:
                ajCallState = AJCallState.AjCallState_Error;
                break;
            case End:
                ajCallState = AJCallState.AjCallState_End;
                break;
            case PausedByRemote:
                ajCallState = AJCallState.AjCallState_PausedByRemote;
                break;
            case UpdatedByRemote:
                ajCallState = AJCallState.AjCallState_UpdatedByRemote;
                break;
            case IncomingEarlyMedia:
                ajCallState = AJCallState.AjCallState_IncomingEarlyMedia;
                break;
            case Updating:
                ajCallState = AJCallState.AjCallState_Updating;
                break;
            case Released:
                ajCallState = AJCallState.AjCallState_Released;
                break;
            case EarlyUpdatedByRemote:
                ajCallState = AJCallState.AjCallState_EarlyUpdatedByRemote;
                break;
            case EarlyUpdating:
                ajCallState = AJCallState.AjCallState_EarlyUpdating;
                break;
        }
        try {
            ajCoreListenerStub.onCallStateChanged(ajCallState, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onTransferStateChanged(Core core, Call call, Call.State state) {

    }

    @Override
    public void onFriendListCreated(Core core, FriendList friendList) {

    }

    @Override
    public void onSubscriptionStateChanged(Core core, Event event, SubscriptionState subscriptionState) {

    }

    @Override
    public void onCallLogUpdated(Core core, CallLog callLog) {

    }


    @Override
    public void onAuthenticationRequested(Core core, AuthInfo authInfo, AuthMethod authMethod) {

    }

    @Override
    public void onNotifyPresenceReceivedForUriOrTel(Core core, Friend friend, String s, PresenceModel presenceModel) {

    }

    @Override
    public void onChatRoomStateChanged(Core core, ChatRoom chatRoom, ChatRoom.State state) {

    }

    @Override
    public void onBuddyInfoUpdated(Core core, Friend friend) {

    }

    @Override
    public void onNetworkReachable(Core core, boolean b) {

    }

    @Override
    public void onNotifyReceived(Core core, Event event, String s, Content content) {

    }

    @Override
    public void onNewSubscriptionRequested(Core core, Friend friend, String s) {

    }


    @Override
    public void onNotifyPresenceReceived(Core core, Friend friend) {

    }

    @Override
    public void onEcCalibrationAudioInit(Core core) {

    }

    @Override
    public void onMessageReceived(Core core, ChatRoom chatRoom, ChatMessage chatMessage) {

    }

    @Override
    public void onEcCalibrationResult(Core core, EcCalibratorStatus ecCalibratorStatus, int i) {

    }

    @Override
    public void onSubscribeReceived(Core core, Event event, String s, Content content) {

    }

    @Override
    public void onInfoReceived(Core core, Call call, InfoMessage infoMessage) {

    }

    @Override
    public void onCallStatsUpdated(Core core, Call call, CallStats callStats) {

    }

    @Override
    public void onFriendListRemoved(Core core, FriendList friendList) {

    }

    @Override
    public void onReferReceived(Core core, String s) {

    }

    @Override
    public void onQrcodeFound(Core core, String s) {

    }

    @Override
    public void onConfiguringStatus(Core core, ConfiguringState configuringState, String s) {

    }

    @Override
    public void onCallCreated(Core core, Call call) {

    }

    @Override
    public void onPublishStateChanged(Core core, Event event, PublishState publishState) {

    }

    @Override
    public void onCallEncryptionChanged(Core core, Call call, boolean b, String s) {

    }

    @Override
    public void onIsComposingReceived(Core core, ChatRoom chatRoom) {

    }

    @Override
    public void onMessageReceivedUnableDecrypt(Core core, ChatRoom chatRoom, ChatMessage chatMessage) {

    }

    @Override
    public void onLogCollectionUploadProgressIndication(Core core, int i, int i1) {

    }

    @Override
    public void onVersionUpdateCheckResultReceived(Core core, VersionUpdateCheckResult versionUpdateCheckResult, String s, String s1) {

    }

    @Override
    public void onEcCalibrationAudioUninit(Core core) {

    }


    @Override
    public void onLogCollectionUploadStateChanged(Core core, Core.LogCollectionUploadState logCollectionUploadState, String s) {

    }

    @Override
    public void onDtmfReceived(Core core, Call call, int i) {

    }
}
