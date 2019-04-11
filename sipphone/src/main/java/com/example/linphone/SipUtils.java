package com.example.linphone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.widget.Toast;


import com.example.linphone.call.CallManager;

import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreException;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;
import org.linphone.core.Transports;
import org.linphone.mediastream.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class SipUtils {
    private boolean isOnLine = false;//linphone是否初始化成功
    private boolean isRegister = false;//linphone是否注册成功
    private Core mCore;
    private static SipUtils instance;
    private Timer mTimer;
    private String userAgentName = "softphone_android";
    private String userAgentversion = "4.0.1";
    private TimerTask lTask;

    public synchronized static SipUtils getIns() {
        if (instance == null) {
            instance = new SipUtils();
        }
        return instance;
    }

    public SipUtils() {
    }

    /**
     * 获取核心类
     *
     * @return
     */
    public synchronized final Core getmCore() {
        return getIns().mCore;
    }

    public final List<Call> getCalls() {
        // return a modifiable list
        return mCore == null ? null : new ArrayList<>(Arrays.asList(mCore.getCalls()));
    }

    public String getUserAgentName() {
        return userAgentName;
    }


    public String getUserAgentversion() {
        return userAgentversion;
    }

    public boolean isRegister() {
        return isRegister;
    }

    public void setRegister(boolean register) {
        isRegister = register;
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler();

    /**
     * 初始化
     */
    @SuppressLint("CheckResult")
    public void init(final WeakReference<Context> applicationContextweakReference, final CoreListenerStub coreListenerStub) {
        if (applicationContextweakReference == null) {
            return;
        }
        final String path = applicationContextweakReference.get().getFilesDir().getAbsolutePath();

        ThreadPoolUtil.getSingleThread().execute(new Runnable() {
            @Override
            public void run() {
                mCore = Factory.instance().createCore(path + "/.linphonerc", path + "/assistant_create.rc", applicationContextweakReference.get());
                mCore.setUserAgent(userAgentName, userAgentversion);
                mCore.addListener(coreListenerStub);
                setSipPore();
                mCore.start();
            }
        });
        lTask = new TimerTask() {
            @SuppressLint("CheckResult")
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCore != null) {
                            mCore.iterate();
                        }
//                        android.util.Log.v("轮训数据打印",String.valueOf(mCore==null));
                    }
                });
            }
        };
        /*use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst after cpu wake up*/
        mTimer = new Timer("Linphone scheduler");
        mTimer.schedule(lTask, 0, 500);

    }

    //port值可以自己指定，-1代表随机
    public void setSipPore() {
        Transports transportPorts = mCore.getTransports();//端口
        transportPorts.setUdpPort(-1);
        transportPorts.setTcpPort(-1);
        transportPorts.setTlsPort(-1);
        mCore.setTransports(transportPorts);

    }

    /**
     * 进行sip信息注册
     * tip 注册是否成功 调用 CoreListenerStub 回调判断
     * SipUtils.registerSip(sipinfo.getUsername(), "", sipinfo.getPassword(), sipinfo.getDisplaynumber(), null,null, sipinfo.getSipurl(), TransportType.Udp);
     */
    public void registerSip(String username,
                               String password,
                               String displayname,
                               String domain) {
        TransportType transport = TransportType.Udp;

        username = getDisplayableUsernameFromAddress(mCore, username);
        domain = getDisplayableUsernameFromAddress(mCore, domain);

        AccountBuilder builder = new AccountBuilder(mCore)
                .setUsername(username)
                .setDomain(domain)
                .setDisplayName(displayname)
                .setPassword(password);
        if (transport != null) {
            builder.setTransport(transport);
        }


        try {
            builder.saveNewAccount();
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }


    public String getDisplayableUsernameFromAddress(Core lc, String sipAddress) {
        String username = sipAddress;
        if (lc == null) return username;

        if (username.startsWith("sip:")) {
            username = username.substring(4);
        }

        if (username.contains("@")) {
            String domain = username.split("@")[1];
            ProxyConfig lpc = lc.getDefaultProxyConfig();
            if (lpc != null) {
                if (domain.equals(lpc.getDomain())) {
                    return username.split("@")[0];
                }
            } else {
                if (false) {
                    return username.split("@")[0];
                }
            }
        }
        return username;
    }

    /**
     * 拨打电话
     *
     * @param context
     * @param number
     */
    public void call(String number, Context context) {
        try {
            if (!acceptCallIfIncomingPending(mCore)) {

                newOutgoingCall(mCore, number, "", context);
            }
        } catch (CoreException e) {
            mCore.terminateCall(mCore.getCurrentCall());
        }

    }

    public static void newOutgoingCall(Core mLc, String to, String displayName, Context context) {
//		if (mLc.inCall()) {
//			listenerDispatcher.tryingNewOutgoingCallButAlreadyInCall();
//			return;
//		}
        if (to == null) return;

        // If to is only a username, try to find the contact to get an alias if existing

        Address lAddress;
        lAddress = mLc.interpretUrl(to); // InterpretUrl does normalizePhoneNumber
        if (lAddress == null) {
            Log.e("Couldn't convert to String to Address : " + to);
            return;
        }

        ProxyConfig lpc = mLc.getDefaultProxyConfig();
        if (false && lpc != null && lAddress.asStringUriOnly().equals(lpc.getIdentityAddress())) {
            return;
        }
        lAddress.setDisplayName(displayName);

        boolean isLowBandwidthConnection = !isHighBandwidthConnection(context);

        if (mLc.isNetworkReachable()) {
            try {
                CallManager.getInstance().inviteAddress(mLc, lAddress, false, isLowBandwidthConnection);


            } catch (CoreException e) {
                Exception exception = e;
                return;
            }
        } else {
            Log.d("linphone ", "Network is unreachable");
        }
    }

    public static boolean isHighBandwidthConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && isConnectionFast(info.getType(), info.getSubtype()));
    }

    public static boolean acceptCallIfIncomingPending(Core mLc) throws CoreException {
        if (mLc.isIncomingInvitePending()) {
            mLc.acceptCall(mLc.getCurrentCall());
            return true;
        }
        return false;
    }

    private static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return false;
            }
        }
        //in doubt, assume connection is good.
        return true;
    }


    /**
     * 接听电话
     *
     * @return
     */
    public boolean answer( Context context) {
        CallParams params = mCore.createCallParams(mCore.getCurrentCall());

        boolean isLowBandwidthConnection = !isHighBandwidthConnection(context);

        if (params != null) {
            params.enableLowBandwidth(isLowBandwidthConnection);
        } else {
            Log.e("Could not create call params for call");
        }

        if (params == null || !acceptCallWithParams(mCore.getCurrentCall(), params)) {
            // the above method takes care of Samsung Galaxy S
            Toast.makeText(context, "接受呼叫时出错", Toast.LENGTH_SHORT).show();
        } else {
            LinphoneManager.getInstance().routeAudioToReceiver();

        }
        return true;
    }

    public boolean acceptCallWithParams(Call call, CallParams params) {
        mCore.acceptCallWithParams(call, params);
        return true;
    }


    public boolean isOnLine() {
        return isOnLine;
    }

    public void setOnLine(boolean onLine) {
        isOnLine = onLine;
    }

    public void signOut() {
        if (instance == null || mCore == null) return;
        LinphoneManager.getInstance().deleteAccount(0);
        ProxyConfig[] prxCfgs = mCore.getProxyConfigList();
        if (prxCfgs != null) for (ProxyConfig prxCfg : prxCfgs) {
            if (prxCfg != null) {
                mCore.removeProxyConfig(prxCfg);
                Address addr = prxCfg.getIdentityAddress();
                if (addr != null) {
                    AuthInfo authInfo = mCore.findAuthInfo(null, addr.getUsername(), addr.getDomain());
                    if (authInfo != null) {
                        mCore.removeAuthInfo(authInfo);
                    }
                }
            }
        }
        mCore.setDefaultProxyConfig(null);
        mCore.clearAllAuthInfo();
        mCore.clearProxyConfig();

        mCore.refreshRegisters();

        LinphoneManager.destroy();
        lTask.cancel();
        lTask=null;

    }

    /**
     * 切换扬声器
     * @param isSpeakers
     */
    public void switchingSpeakers(boolean isSpeakers){
        if (LinphoneManager.getInstance()!=null){
            if (isSpeakers) {
                LinphoneManager.getInstance().routeAudioToSpeaker();
            } else {
                LinphoneManager.getInstance().routeAudioToReceiver();
            }
        }else {
            throw new NullPointerException("LinphoneManager为初始化");
        }

    }
    /**
     * 挂断电话
     */
    public void hangUp() {
        if (mCore != null) {
            if (mCore.isInConference()) {
                mCore.terminateConference();
            } else {
                mCore.terminateAllCalls();
            }
        }else {
            throw new NullPointerException("核心类core为空");
        }

    }

    /**
     * 通话是否静音
     * @param isMicMuted
     */
    public void switchingMute(boolean isMicMuted) {
        if (mCore!=null){
            mCore.enableMic(isMicMuted);
        }else {
            throw new NullPointerException("核心类core为空");
        }
    }
}
