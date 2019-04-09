package com.example.linphone;

/*
LinphoneManager.java
Copyright (C) 2018  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;


import org.linphone.core.AccountCreator;
import org.linphone.core.AccountCreatorListener;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.AuthMethod;
import org.linphone.core.Call;
import org.linphone.core.Call.State;
import org.linphone.core.CallLog;
import org.linphone.core.CallStats;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.ConfiguringState;
import org.linphone.core.Content;
import org.linphone.core.Core;
import org.linphone.core.Core.LogCollectionUploadState;
import org.linphone.core.CoreException;
import org.linphone.core.CoreListener;
import org.linphone.core.EcCalibratorStatus;
import org.linphone.core.Event;
import org.linphone.core.Factory;
import org.linphone.core.Friend;
import org.linphone.core.FriendList;
import org.linphone.core.GlobalState;
import org.linphone.core.InfoMessage;
import org.linphone.core.LogCollectionState;
import org.linphone.core.LogLevel;
import org.linphone.core.LoggingService;
import org.linphone.core.LoggingServiceListener;
import org.linphone.core.PresenceActivity;
import org.linphone.core.PresenceBasicStatus;
import org.linphone.core.PresenceModel;
import org.linphone.core.ProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.Reason;
import org.linphone.core.RegistrationState;
import org.linphone.core.SubscriptionState;
import org.linphone.core.VersionUpdateCheckResult;
import org.linphone.core.VideoActivationPolicy;
import org.linphone.mediastream.Log;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration.AndroidCamera;
import org.linphone.mediastream.video.capture.hwconf.Hacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.media.AudioManager.MODE_RINGTONE;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_VOICE_CALL;

/**
 * Manager of the low level LibLinphone stuff.<br />
 * Including:<ul>
 * <li>Starting C liblinphone</li>
 * <li>Reacting to C liblinphone state changes</li>
 * <li>Calling Linphone android service listener methods</li>
 * <li>Interacting from Android GUI/service with low level SIP stuff/</li>
 * </ul>
 * <p>
 * Add Service Listener to react to Linphone state changes.
 */
public class LinphoneManager implements  SensorEventListener, AccountCreatorListener, CoreListener {

    private static LinphoneManager instance;
    private Context mServiceContext;
    private AudioManager mAudioManager;
    private PowerManager mPowerManager;
    private Resources mR;
    private Core mLc;
    private String basePath;
    private static boolean sExited;
    private boolean mAudioFocused;
    private boolean callGsmON;
    private WakeLock mProximityWakelock;
    private AccountCreator accountCreator;
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private boolean mProximitySensingEnabled;
    private boolean handsetON = false;

    public String wizardLoginViewDomain = null;

    protected LinphoneManager(final Context c) {
        sExited = false;
        mServiceContext = c;
        basePath = c.getFilesDir().getAbsolutePath();
        mLPConfigXsd = basePath + "/lpconfig.xsd";
        mLinphoneFactoryConfigFile = basePath + "/linphonerc";
        mConfigFile = basePath + "/.linphonerc";
        mDynamicConfigFile = basePath + "/assistant_create.rc";
        mChatDatabaseFile = basePath + "/linphone-history.db";
        mCallLogDatabaseFile = basePath + "/linphone-log-history.db";
        mFriendsDatabaseFile = basePath + "/linphone-friends.db";
        mRingSoundFile = basePath + "/ringtone.mkv";
        mUserCertsPath = basePath + "/user-certs";

        mAudioManager = ((AudioManager) c.getSystemService(Context.AUDIO_SERVICE));
        mVibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        mPowerManager = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mR = c.getResources();
        
        File f=new File(mUserCertsPath);
        if(!f.exists()){
            if(!f.mkdir()){
                       Log.e(mUserCertsPath+" can't be created.");
            }
        }
        mLc=SipUtils.getIns().getmCore();
        mMediaScanner = new LinphoneMediaScanner(c);
    }

    private static final int LINPHONE_VOLUME_STREAM = STREAM_VOICE_CALL;
    private static final int dbStep = 4;
    /**
     * Called when the activity is first created.
     */
    private final String mLPConfigXsd;
    private final String mLinphoneFactoryConfigFile;
    private final String mDynamicConfigFile;
    public final String mConfigFile;
    private final String mChatDatabaseFile;
    private final String mRingSoundFile;
    private final String mCallLogDatabaseFile;
    private final String mFriendsDatabaseFile;
    private final String mUserCertsPath;
    private LinphoneMediaScanner mMediaScanner;

    private void routeAudioToSpeakerHelper(boolean speakerOn) {
        Log.w("Routing audio to " + (speakerOn ? "speaker" : "earpiece") + ", disabling bluetooth audio route");

        enableSpeaker(speakerOn);
    }

    public boolean isSpeakerEnabled() {
        return mAudioManager != null && mAudioManager.isSpeakerphoneOn();
    }

    /**
     * 是否开启免提
     * @param enable
     */
    public void enableSpeaker(boolean enable) {
        mAudioManager.setSpeakerphoneOn(enable);
        mAudioManager.setMode(enable?AudioManager.MODE_NORMAL:AudioManager.MODE_IN_COMMUNICATION);
    }



    public void routeAudioToSpeaker() {
        routeAudioToSpeakerHelper(true);
    }

    public void routeAudioToReceiver() {
        routeAudioToSpeakerHelper(false);
    }


    private boolean isPresenceModelActivitySet() {
        Core lc = getLcIfManagerNotDestroyedOrNull();
        if (isInstanciated() && lc != null) {
            return lc.getPresenceModel() != null && lc.getPresenceModel().getActivity() != null;
        }
        return false;
    }

    public void changeStatusToOnline() {
        Core lc = getLcIfManagerNotDestroyedOrNull();
        if (lc == null) return;
        PresenceModel model = lc.createPresenceModel();
        model.setBasicStatus(PresenceBasicStatus.Open);
        lc.setPresenceModel(model);
		/*
		if (isInstanciated() && lc != null && isPresenceModelActivitySet() && lc.getPresenceModel().getActivity().getType() != PresenceActivity.Type.TV) {
			lc.getPresenceModel().getActivity().setType(PresenceActivity.Type.TV);
		} else if (isInstanciated() && lc != null && !isPresenceModelActivitySet()) {
			PresenceModel model = lc.createPresenceModelWithActivity(PresenceActivity.Type.TV, null);
			lc.setPresenceModel(model);
		}
		*/
    }
    @SuppressLint("InvalidWakeLockTag")
    public synchronized void initLiblinphone(Core lc) throws CoreException {
        mLc = lc;

        mLc.setZrtpSecretsFile(basePath + "/zrtp_secrets");
        try {
            mLc.setUserAgent(SipUtils.getIns().getUserAgentName(), SipUtils.getIns().getUserAgentversion());
        } catch (Exception e) {
            Log.e(e, "cannot get version name");
        }

        mLc.setChatDatabasePath(mChatDatabaseFile);
        mLc.setCallLogsDatabasePath(mCallLogDatabaseFile);
        mLc.setFriendsDatabasePath(mFriendsDatabaseFile);
        mLc.setUserCertificatesPath(mUserCertsPath);
        //mLc.setCallErrorTone(Reason.NotFound, mErrorToneFile);
        enableDeviceRingtone(true);

        int availableCores = Runtime.getRuntime().availableProcessors();
        Log.w("MediaStreamer : " + availableCores + " cores detected and configured");
        //mLc.setCpuCount(availableCores);

        mLc.migrateLogsFromRcToDb();

        // Migrate existing linphone accounts to have conference factory uri set


        initPushNotificationsService();

        mProximityWakelock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "manager_proximity_sensor");


        resetCameraFromPreferences();

        accountCreator = LinphoneManager.getLc().createAccountCreator(getXmlrpcUrl());
        accountCreator.setListener(this);
        callGsmON = false;
        mLc.addListener(this);
    }
    private void initPushNotificationsService() {
        try {
            Class<?> firebaseClass = Class.forName("com.google.firebase.iid.FirebaseInstanceId");
            Object firebaseInstance = firebaseClass.getMethod("getInstance").invoke(null);
            final String refreshedToken = (String) firebaseClass.getMethod("getToken").invoke(firebaseInstance);

            //final String refreshedToken = com.google.firebase.iid.FirebaseInstanceId.getInstance().getToken();
            if (refreshedToken != null) {
                Log.i("[Push Notification] init push notif service token is: " + refreshedToken);
                setPushNotificationRegistrationID(refreshedToken);
            }
        } catch (Exception e) {
            Log.i("[Push Notification] firebase not available.");
        }
    }
    public void setPushNotificationRegistrationID(String regId) {
        if (getLc().getConfig() == null) return;
        Log.i("[Push Notification] New token received" + regId);
        getLc().getConfig().setString("app", "push_notification_regid", (regId != null) ? regId : "");
        setPushNotificationEnabled(isPushNotificationEnabled());
    }
    public boolean isPushNotificationEnabled() {
        return getLc().getConfig().getBool("app", "push_notification", true);
    }
    public String getXmlrpcUrl() {
        return getLc().getConfig().getString("assistant", "xmlrpc_url", null);
    }
    public void setPushNotificationEnabled(boolean enable) {
        getLc().getConfig().setBool("app", "push_notification", enable);

        Core lc = getLc();
        if (lc == null) {
            return;
        }

        if (enable) {
            // Add push infos to exisiting proxy configs
            String regId = getPushNotificationRegistrationID();
            String appId = "929724111839";
            if (regId != null && lc.getProxyConfigList().length > 0) {
                for (ProxyConfig lpc : lc.getProxyConfigList()) {
                    if (lpc == null) continue;
                    if (!lpc.isPushNotificationAllowed()) {
                        lpc.edit();
                        lpc.setContactUriParameters(null);
                        lpc.done();
                        if (lpc.getIdentityAddress() != null)
                            Log.d("Push notif infos removed from proxy config " + lpc.getIdentityAddress().asStringUriOnly());
                    } else {
                        String contactInfos = "app-id=" + appId + ";pn-type=push_type" + ";pn-tok=" + regId + ";pn-silent=1";
                        String prevContactParams = lpc.getContactParameters();
                        if (prevContactParams == null || prevContactParams.compareTo(contactInfos) != 0) {
                            lpc.edit();
                            lpc.setContactUriParameters(contactInfos);
                            lpc.done();
                            if (lpc.getIdentityAddress() != null)
                                Log.d("Push notif infos added to proxy config " + lpc.getIdentityAddress().asStringUriOnly());
                        }
                    }
                }
                Log.i("[Push Notification] Refreshing registers to ensure token is up to date" + regId);
                lc.refreshRegisters();
            }
        } else {
            if (lc.getProxyConfigList().length > 0) {
                for (ProxyConfig lpc : lc.getProxyConfigList()) {
                    lpc.edit();
                    lpc.setContactUriParameters(null);
                    lpc.done();
                    if (lpc.getIdentityAddress() != null)
                        Log.d("Push notif infos removed from proxy config " + lpc.getIdentityAddress().asStringUriOnly());
                }
                lc.refreshRegisters();
            }
        }
    }
    public String getPushNotificationRegistrationID() {
        return getLc().getConfig().getString("app", "push_notification_regid", null);
    }

    public void changeStatusToOnThePhone() {
        Core lc = getLcIfManagerNotDestroyedOrNull();
        if (lc == null) return;

        if (isInstanciated() && isPresenceModelActivitySet() && lc.getPresenceModel().getActivity().getType() != PresenceActivity.Type.OnThePhone) {
            lc.getPresenceModel().getActivity().setType(PresenceActivity.Type.OnThePhone);
        } else if (isInstanciated() && !isPresenceModelActivitySet()) {
            PresenceModel model = lc.createPresenceModelWithActivity(PresenceActivity.Type.OnThePhone, null);
            lc.setPresenceModel(model);
        }
    }

    public void changeStatusToOffline() {
        Core lc = getLcIfManagerNotDestroyedOrNull();
        if (isInstanciated() && lc != null) {
            PresenceModel model = lc.getPresenceModel();
            model.setBasicStatus(PresenceBasicStatus.Closed);
            lc.setPresenceModel(model);
        }
    }

    public void subscribeFriendList(boolean enabled) {
        Core lc = getLcIfManagerNotDestroyedOrNull();
        if (lc != null && lc.getFriendsLists() != null && lc.getFriendsLists().length > 0) {
            FriendList mFriendList = (lc.getFriendsLists())[0];
            Log.i("Presence list subscription is " + (enabled ? "enabled" : "disabled"));
            mFriendList.enableSubscriptions(enabled);
        }
    }


    public static synchronized final LinphoneManager getInstance(Context applicationContext) {
        if (instance==null){
            instance = new LinphoneManager(applicationContext);
        }
        return instance;
    }

    public static synchronized final LinphoneManager getInstance() {
        return instance;
    }
    public static synchronized final Core getLc() {
        return getInstance().mLc;
    }

    private void resetCameraFromPreferences() {
        int camId = 0;
        AndroidCamera[] cameras = AndroidCameraConfiguration.retrieveCameras();
        for (AndroidCamera androidCamera : cameras) {
            if (androidCamera.frontFacing == true)
                camId = androidCamera.id;
        }
        String[] devices = getLc().getVideoDevicesList();
        String newDevice = devices[camId];
        LinphoneManager.getLc().setVideoDevice(newDevice);
    }

    public void playDtmf(ContentResolver r, char dtmf) {
        try {
            if (Settings.System.getInt(r, Settings.System.DTMF_TONE_WHEN_DIALING) == 0) {
                // audible touch disabled: don't play on speaker, only send in outgoing stream
                return;
            }
        } catch (SettingNotFoundException e) {
        }

        getLc().playDtmf(dtmf, -1);
    }

    public void terminateCall() {
        if (mLc.inCall()) {
            mLc.terminateCall(mLc.getCurrentCall());
        }
    }

    public synchronized final void destroyCore() {
        sExited = true;
        try {
            destroyLinphoneCore();
        } catch (RuntimeException e) {
            Log.e(e);
        } finally {

            mLc = null;
        }
    }

    public boolean isVideoEnabled() {
        if (getLc() == null) return false;
        return getLc().videoSupported() && getLc().videoEnabled();
    }


    public boolean isHansetModeOn() {
        return handsetON;
    }

    public void copyFromPackage(int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = mServiceContext.openFileOutput(target, 0);
        InputStream lInputStream = mR.openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }

    //public void loadConfig(){
    //	try {
    //		copyIfNotExist(R.raw.configrc, mConfigFile);
    //	} catch (Exception e){
    //		Log.w(e);
    //	}
    //	LinphonePreferences.instance().setRemoteProvisioningUrl("file://" + mConfigFile);
    //	getLc().getConfig().setInt("misc","transient_provisioning",1);
    //}

    private void destroyLinphoneCore() {
        mLc.setNetworkReachable(false);
        mLc = null;
    }

    public void enableProximitySensing(boolean enable) {
        if (enable) {
            if (!mProximitySensingEnabled) {
                mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
                mProximitySensingEnabled = true;
            }
        } else {
            if (mProximitySensingEnabled) {
                mSensorManager.unregisterListener(this);
                mProximitySensingEnabled = false;
                // Don't forgeting to release wakelock if held
                if (mProximityWakelock.isHeld()) {
                    mProximityWakelock.release();
                }
            }
        }
    }

    public static Boolean isProximitySensorNearby(final SensorEvent event) {
        float threshold = 4.001f; // <= 4 cm is near

        final float distanceInCm = event.values[0];
        final float maxDistance = event.sensor.getMaximumRange();
        Log.d("Proximity sensor report [" + distanceInCm + "] , for max range [" + maxDistance + "]");

        if (maxDistance <= threshold) {
            // Case binary 0/1 and short sensors
            threshold = maxDistance;
        }
        return distanceInCm < threshold;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //只有通话中时需要贴近息屏离开亮屏
        if (event.timestamp == 0||mLc==null||mLc.getCurrentCall()==null||mLc.getCurrentCall().getState()!= State.StreamsRunning) return;
        if (isProximitySensorNearby(event)) {
            if (!mProximityWakelock.isHeld()) {
                mProximityWakelock.acquire();
            }
        } else {
            if (mProximityWakelock.isHeld()) {
                mProximityWakelock.release();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    public static synchronized void destroy() {
        if (instance == null) return;
        instance.changeStatusToOffline();
        instance.mMediaScanner.destroy();
        sExited = true;
        instance.destroyCore();
        instance = null;
    }

    public LinphoneMediaScanner getMediaScanner() {
        return mMediaScanner;
    }

    private String getString(int key) {
        return mR.getString(key);
    }

    private Call ringingCall;

    private MediaPlayer mRingerPlayer;
    private Vibrator mVibrator;

    public void onNewSubscriptionRequested(Core lc, Friend lf, String url) {
    }

    public void onNotifyPresenceReceived(Core lc, Friend lf) {
    }

    @Override
    public void onEcCalibrationAudioInit(Core core) {

    }

    @Override
    public void onMessageReceived(Core core, ChatRoom chatRoom, ChatMessage chatMessage) {

    }


    @Override
    public void onEcCalibrationResult(Core lc, EcCalibratorStatus status, int delay_ms) {
        ((AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE)).setMode(AudioManager.MODE_NORMAL);
        mAudioManager.abandonAudioFocus(null);
        Log.i("Set audio mode on 'Normal'");
    }

    @Override
    public void onSubscribeReceived(Core core, Event event, String s, Content content) {

    }

    @Override
    public void onInfoReceived(Core core, Call call, InfoMessage infoMessage) {

    }


    public void onRegistrationStateChanged(final Core lc, final ProxyConfig proxy, final RegistrationState state, final String message) {
        Log.i("New registration state [" + state + "]");
        if (LinphoneManager.getLc().getDefaultProxyConfig() == null) {
            subscribeFriendList(false);
        }
    }

    public Context getContext() {

        return mServiceContext ;
    }

    public void setAudioManagerInCallMode() {
        if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            Log.w("[AudioManager] already in MODE_IN_COMMUNICATION, skipping...");
            return;
        }
        Log.d("[AudioManager] Mode: MODE_IN_COMMUNICATION");

        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    @Override
    public void onTransferStateChanged(Core core, Call call, State state) {

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

    @SuppressLint("Wakelock")
    public void onCallStateChanged(final Core lc, final Call call, final State state, final String message) {
        if (state == State.OutgoingInit) {
            //Enter the MODE_IN_COMMUNICATION mode as soon as possible, so that ringback
            //is heard normally in earpiece or bluetooth receiver.
            setAudioManagerInCallMode();
            requestAudioFocus(STREAM_VOICE_CALL);
            startBluetooth();
        }

        //已有接入的电话，则需要挂断
        if (state == State.IncomingReceived && getCallGsmON()) {
            if (mLc != null) {
                mLc.declineCall(call, Reason.Busy);
            }
        }  else if (state == State.IncomingReceived || (state == State.IncomingEarlyMedia )) {
            // Brighten screen for at least 10 seconds
            if (mLc.getCallsNb() == 1) {
                requestAudioFocus(STREAM_RING);

                ringingCall = call;
                startRinging();
                // otherwise there is the beep
            }
        } else if (call == ringingCall && isRinging) {
            //previous state was ringing, so stop ringing
            stopRinging();
        }

        if (state == State.Connected) {
            if (mLc.getCallsNb() == 1) {
                //It is for incoming calls, because outgoing calls enter MODE_IN_COMMUNICATION immediately when they start.
                //However, incoming call first use the MODE_RINGING to play the local ring.
                if (call.getDir() == Call.Dir.Incoming) {
                    setAudioManagerInCallMode();
                    //mAudioManager.abandonAudioFocus(null);
                    requestAudioFocus(STREAM_VOICE_CALL);
                }
            }

            if (Hacks.needSoftvolume()) {
                Log.w("Using soft volume audio hack");
                adjustVolume(0); // Synchronize
            }
        }

        if (state == State.StreamsRunning) {
            startBluetooth();
            setAudioManagerInCallMode();
        }
        if (state == State.End || state == State.Error) {
            if (mLc.getCallsNb() == 0) {
                //Disabling proximity sensor
                enableProximitySensing(false);
                Context activity = getContext();
                if (mAudioFocused) {
                    int res = mAudioManager.abandonAudioFocus(null);
                    Log.d("Audio focus released a bit later: " + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "Granted" : "Denied"));
                    mAudioFocused = false;
                }
                if (activity != null) {
                    TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
                    if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                        Log.d("---AudioManager: back to MODE_NORMAL");
                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                        Log.d("All call terminated, routing back to earpiece");
                        routeAudioToReceiver();
                    }
                }
            }
        }
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

    public void startBluetooth() {

    }

    public void onCallStatsUpdated(final Core lc, final Call call, final CallStats stats) {
    }

    @Override
    public void onFriendListRemoved(Core core, FriendList friendList) {

    }

    @Override
    public void onReferReceived(Core core, String s) {

    }

    public void onCallEncryptionChanged(Core lc, Call call,
                                        boolean encrypted, String authenticationToken) {
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
    public void onGlobalStateChanged(Core core, GlobalState globalState, String s) {

    }

    @Override
    public void onLogCollectionUploadStateChanged(Core core, LogCollectionUploadState logCollectionUploadState, String s) {

    }

    @Override
    public void onDtmfReceived(Core core, Call call, int i) {

    }


    private boolean isRinging;

    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            int res = mAudioManager.requestAudioFocus(null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            Log.d("Audio focus requested: " + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "Granted" : "Denied"));
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
        }
    }

    public void enableDeviceRingtone(boolean use) {
        if (use) {
            mLc.setRing(null);
        } else {
            mLc.setRing(mRingSoundFile);
        }
    }
    public String getRingtone(String defaultRingtone) {
        String ringtone = getLc().getConfig().getString("app", "ringtone", defaultRingtone);
        if (ringtone == null || ringtone.length() == 0)
            ringtone = defaultRingtone;
        return ringtone;
    }
    private synchronized void startRinging() {

        routeAudioToSpeaker();

        //if (Hacks.needGalaxySAudioHack())
        mAudioManager.setMode(MODE_RINGTONE);

        try {
            if ((mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
                    && mVibrator != null) {
                long[] patern = {0, 1000, 1000};
                mVibrator.vibrate(patern, 1);
            }
            if (mRingerPlayer == null) {
                requestAudioFocus(STREAM_RING);
                mRingerPlayer = new MediaPlayer();
                mRingerPlayer.setAudioStreamType(STREAM_RING);

                String ringtone = getRingtone(Settings.System.DEFAULT_RINGTONE_URI.toString());
                try {
                    if (ringtone.startsWith("content://")) {
                        mRingerPlayer.setDataSource(mServiceContext, Uri.parse(ringtone));
                    } else {
                        FileInputStream fis = new FileInputStream(ringtone);
                        mRingerPlayer.setDataSource(fis.getFD());
                        fis.close();
                    }
                } catch (IOException e) {
                    Log.e(e, "Cannot set ringtone");
                }

                mRingerPlayer.prepare();
                mRingerPlayer.setLooping(true);
                mRingerPlayer.start();
            } else {
                Log.w("already ringing");
            }
        } catch (Exception e) {
            Log.e(e, "cannot handle incoming call");
        }
        isRinging = true;
    }

    private synchronized void stopRinging() {
        if (mRingerPlayer != null) {
            mRingerPlayer.stop();
            mRingerPlayer.release();
            mRingerPlayer = null;
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }

        if (Hacks.needGalaxySAudioHack())
            mAudioManager.setMode(AudioManager.MODE_NORMAL);

        isRinging = false;

    }


    public void adjustVolume(int i) {
        if (Build.VERSION.SDK_INT < 15) {
            int oldVolume = mAudioManager.getStreamVolume(LINPHONE_VOLUME_STREAM);
            int maxVolume = mAudioManager.getStreamMaxVolume(LINPHONE_VOLUME_STREAM);

            int nextVolume = oldVolume + i;
            if (nextVolume > maxVolume) nextVolume = maxVolume;
            if (nextVolume < 0) nextVolume = 0;

            mLc.setPlaybackGainDb((nextVolume - maxVolume) * dbStep);
        } else
            // starting from ICS, volume must be adjusted by the application, at least for STREAM_VOICE_CALL volume stream
            mAudioManager.adjustStreamVolume(LINPHONE_VOLUME_STREAM, i < 0 ? AudioManager.ADJUST_LOWER : AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    public static synchronized Core getLcIfManagerNotDestroyedOrNull() {
        if (sExited || instance == null) {
            // Can occur if the UI thread play a posted event but in the meantime the LinphoneManager was destroyed
            // Ex: stop call and quickly terminate application.
            return null;
        }
        return getLc();
    }

    public static final boolean isInstanciated() {
        return instance != null;
    }


    public boolean getCallGsmON() {
        return callGsmON;
    }

    public void setCallGsmON(boolean on) {
        callGsmON = on;
    }


    @Override
    public void onConfiguringStatus(Core lc,
                                    ConfiguringState state, String message) {
        Log.d("Remote provisioning status = " + state.toString() + " (" + message + ")");

        if (state == ConfiguringState.Successful) {
            ProxyConfig proxyConfig = lc.createProxyConfig();
            Address addr = proxyConfig.getIdentityAddress();
            wizardLoginViewDomain = addr.getDomain();
        }
    }

    @Override
    public void onCallCreated(Core core, Call call) {

    }

    @Override
    public void onPublishStateChanged(Core core, Event event, PublishState publishState) {

    }


    @Override
    public void onIsAccountExist(AccountCreator accountCreator, AccountCreator.Status status, String resp) {
        if (status.equals(AccountCreator.Status.AccountExist)) {
            accountCreator.isAccountLinked();
        }
    }

    @Override
    public void onCreateAccount(AccountCreator accountCreator, AccountCreator.Status status, String resp) {
    }

    @Override
    public void onActivateAccount(AccountCreator accountCreator, AccountCreator.Status status, String resp) {
    }

    @Override
    public void onLinkAccount(AccountCreator accountCreator, AccountCreator.Status status, String resp) {
        if (status.equals(AccountCreator.Status.AccountNotLinked)) {
        }
    }

    @Override
    public void onActivateAlias(AccountCreator accountCreator, AccountCreator.Status status, String resp) {
    }

    @Override
    public void onIsAccountActivated(AccountCreator accountCreator, AccountCreator.Status status, String resp) {
    }

    @Override
    public void onRecoverAccount(AccountCreator accountCreator, AccountCreator.Status status, String resp) {
    }

    @Override
    public void onIsAccountLinked(AccountCreator accountCreator, AccountCreator.Status status, String resp) {
        if (status.equals(AccountCreator.Status.AccountNotLinked)) {
        }
    }

    @Override
    public void onIsAliasUsed(AccountCreator accountCreator, AccountCreator.Status status, String resp) {
    }

    @Override
    public void onUpdateAccount(AccountCreator accountCreator, AccountCreator.Status status, String resp) {

    }

    public void onQrcodeFound(Core lc, String something) {
    }
    public boolean shouldAutomaticallyAcceptVideoRequests() {
        if (getLc() == null) return false;
        VideoActivationPolicy vap = getLc().getVideoActivationPolicy();
        return vap.getAutomaticallyAccept();
    }

    public boolean onKeyVolumeAdjust(int keyCode) {
        if (!((keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                && (Hacks.needSoftvolume()) || Build.VERSION.SDK_INT >= 15)) {
            return false; // continue
        }

        if (!SipUtils.getIns().isOnLine()) {
            Log.i("Couldn't change softvolume has service is not running");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            LinphoneManager.getInstance().adjustVolume(1);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            LinphoneManager.getInstance().adjustVolume(-1);
        }
        return true;
    }

    public static boolean onKeyBackGoHome(Activity activity, int keyCode, KeyEvent event) {
        if (!(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
            return false; // continue
        }

        activity.startActivity(new Intent()
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME));
        return true;
    }

    public String getAddressDisplayName(Address address) {
        if (address == null) return null;

        String displayName = address.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = address.getUsername();
        }
        if (displayName == null || displayName.isEmpty()) {
            displayName = address.asStringUriOnly();
        }
        return displayName;
    }

    public void resetDefaultProxyConfig() {
        if (getLc() == null) return;
        int count = getLc().getProxyConfigList().length;
        for (int i = 0; i < count; i++) {
            if (isAccountEnabled(i)) {
                getLc().setDefaultProxyConfig(getProxyConfig(i));
                break;
            }
        }

        if (getLc().getDefaultProxyConfig() == null) {
            getLc().setDefaultProxyConfig(getProxyConfig(0));
        }
    }

    public void deleteAccount(int n) {
        if (getLc() == null) return;
        ProxyConfig proxyCfg = getProxyConfig(n);
        if (proxyCfg != null)
            getLc().removeProxyConfig(proxyCfg);
        if (getLc().getProxyConfigList().length != 0) {
            resetDefaultProxyConfig();
        } else {
            getLc().setDefaultProxyConfig(null);
        }

        AuthInfo authInfo = getAuthInfo(n);
        if (authInfo != null) {
            getLc().removeAuthInfo(authInfo);
        }

        getLc().refreshRegisters();
    }
    public boolean isAccountEnabled(int n) {
        return getProxyConfig(n).registerEnabled();
    }
    // Accounts settings
    private ProxyConfig getProxyConfig(int n) {
        if (getLc() == null) return null;
        ProxyConfig[] prxCfgs = getLc().getProxyConfigList();
        if (n < 0 || n >= prxCfgs.length)
            return null;
        return prxCfgs[n];
    }
    private AuthInfo getAuthInfo(int n) {
        ProxyConfig prxCfg = getProxyConfig(n);
        if (prxCfg == null) return null;
        Address addr = prxCfg.getIdentityAddress();
        AuthInfo authInfo = getLc().findAuthInfo(null, addr.getUsername(), addr.getDomain());
        return authInfo;
    }
    public int getDefaultAccountIndex() {
        if (getLc() == null)
            return -1;
        ProxyConfig defaultPrxCfg = getLc().getDefaultProxyConfig();
        if (defaultPrxCfg == null)
            return -1;

        ProxyConfig[] prxCfgs = getLc().getProxyConfigList();
        for (int i = 0; i < prxCfgs.length; i++) {
            if (defaultPrxCfg.getIdentityAddress().equals(prxCfgs[i].getIdentityAddress())) {
                return i;
            }
        }
        return -1;
    }

    /**
     *
     * @param isUseJavaLogger
     * @param isDebugEnabled 打印各个过程中c代码运行日志
     * @param appName
     */
    public static void initLoggingService(boolean isUseJavaLogger,boolean isDebugEnabled, String appName) {
        if (!isUseJavaLogger) {
            Factory.instance().enableLogCollection(LogCollectionState.Enabled);
            Factory.instance().setDebugMode(isDebugEnabled, appName);
        } else {
            Factory.instance().setDebugMode(isDebugEnabled, appName);
            Factory.instance().enableLogCollection(LogCollectionState.EnabledWithoutPreviousLogHandler);
            Factory.instance().getLoggingService().setListener(new LoggingServiceListener() {
                @Override
                public void onLogMessageWritten(LoggingService logService, String domain, LogLevel lev, String message) {
                    switch (lev) {
                        case Debug:
                            android.util.Log.d(domain, message);
                            break;
                        case Message:
                            android.util.Log.i(domain, message);
                            break;
                        case Warning:
                            android.util.Log.w(domain, message);
                            break;
                        case Error:
                            android.util.Log.e(domain, message);
                            break;
                        case Fatal:
                        default:
                            android.util.Log.wtf(domain, message);
                            break;
                    }
                }
            });
        }
    }

}
