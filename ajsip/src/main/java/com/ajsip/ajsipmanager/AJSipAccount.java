package com.ajsip.ajsipmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.ajsip.AccountBuilder;
import com.ajsip.LinphoneManager;
import com.ajsip.ThreadPoolUtil;
import com.ajsip.callback.AJCoreListenerStub;
import com.ajsip.callback.MyCoreListenerStub;

import org.linphone.core.AuthInfo;
import org.linphone.core.Core;
import org.linphone.core.CoreException;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;
import org.linphone.core.Transports;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AJSipAccount {

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler();
    private Core mCore;
    private TimerTask lTask;
    private Timer mTimer;
    /**
     * 初始化
     */
    @SuppressLint("CheckResult")
    public void init(Context context, String userAgentName, String userAgentversion, final AJCoreListenerStub ajCoreListenerStub) {
        if (context == null) {
            return;
        }
        final String path = context.getFilesDir().getAbsolutePath();

        ThreadPoolUtil.getSingleThread().execute(new Runnable() {
            @Override
            public void run() {
                mCore = Factory.instance().createCore(path + "/.linphonerc", path + "/assistant_create.rc", context);
                mCore.setUserAgent(userAgentName, userAgentversion);
                mCore.addListener(new MyCoreListenerStub(ajCoreListenerStub));
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
                    }
                });
            }
        };
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
                            String domain, Map<String,String> headMap,Context context) {
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
            builder.saveNewAccount(headMap);
        } catch (CoreException e) {
            e.printStackTrace();
        }
        try {
            LinphoneManager.getInstance(context).initLiblinphone(mCore);
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
    public void signOut() {
        LinphoneManager.getInstance().signOut();
        lTask.cancel();
        lTask = null;
        mCore=null;
    }
    public String getDomain(String name){
        return LinphoneManager.getInstance().getDomain(name);
    }
    public String getUserAgent() {
        return LinphoneManager.getInstance().getUserAgent();
    }
}
