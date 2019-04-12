package com.example.linphonetest;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.linphone.LinphoneManager;
import com.example.linphone.SipUtils;
import com.example.linphonetest.databinding.ActivityMainBinding;

import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreException;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.GlobalState;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String username = "69802008619";
    String password = "101944957262#";
    String displayname = "17778115596";
    String domain = "cxc32.italkbb.com:10000";
    //        String username="69802845066";
//        String password="298613210000#";
//        String displayname="15235661546";
//        String domain="cxc32.italkbb.com:10000";
//        String username="69801006278";
//        String password="101204375147#";
//        String displayname="15235661546";
//        String domain="ose7.italkbb.com:10000";
    ActivityMainBinding binding;
    private boolean isSpeakerEnabled = false, isMicMuted = false,isPlayKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setClick(this);
        String[] strings = {Manifest.permission.RECORD_AUDIO};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(strings, 0);
        }
        Objects.requireNonNull(getSupportActionBar()).hide();
        LinphoneManager.initLoggingService(true, true, getString(R.string.app_name));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linphone_tv1:
                long time1 = System.currentTimeMillis();
                SipUtils.getIns().init(new WeakReference<Context>(this), coreListener);
                Log.d("linphone_初始化时间=", System.currentTimeMillis() - time1 + "");
                break;
            case R.id.linphone_tv2:
                SipUtils.getIns().call("69801006278", MainActivity.this);
                break;
            case R.id.linphone_tv3:
                SipUtils.getIns().hangUp();
                break;
            case R.id.linphone_tv4:
                SipUtils.getIns().answer( MainActivity.this);
                break;
            case R.id.linphone_tv5:
                isMicMuted = !isMicMuted;
                SipUtils.getIns().switchingMute(!isMicMuted);
                binding.linphoneTv5.setText("静音" + String.valueOf(isMicMuted));
                break;
            case R.id.linphone_tv6:
                isSpeakerEnabled = !isSpeakerEnabled;
                SipUtils.getIns().switchingSpeakers(isSpeakerEnabled);
                binding.linphoneTv6.setText("免提" + String.valueOf(this.isSpeakerEnabled));

                break;
            case R.id.linphone_tv7:
                SipUtils.getIns().signOut();
                break;
            case R.id.linphone_tv8:
                if (isPlayKey){
                    SipUtils.getIns().stopDtmf();
                }else {
                    SipUtils.getIns().playDtmf(getContentResolver(), '6');
                }
                isPlayKey=!isPlayKey;
                break;
            case R.id.linphone_tv9:
                SipUtils.getIns().sendDtmf('6');
                break;
        }
    }

    CoreListenerStub coreListener = new CoreListenerStub() {
        @Override
        public void onGlobalStateChanged(Core lc, GlobalState gstate, String message) {
            super.onGlobalStateChanged(lc, gstate, message);
            Log.d("linphone_初始化状态=", gstate.name() + "===" + message);
            binding.setInitState("初始化状态"+message);
            /**
             * 初始化成功
             */
            SipUtils.getIns().setOnLine(gstate == GlobalState.On);
            if (gstate == GlobalState.On) {
                long time1 = System.currentTimeMillis();
                SipUtils.getIns().registerSip(username,
                        password,
                        displayname,
                        domain,new WeakReference<Context>(MainActivity.this));

            }
        }

        @Override
        public void onCallStateChanged(Core lc, Call call, Call.State cstate, String message) {
            super.onCallStateChanged(lc, call, cstate, message);
            Log.d("linphone_通话状态=", cstate.name() + "===" + message);
            //通话状态
            binding.setCallstate("通话状态"+message);

        }

        @Override
        public void onRegistrationStateChanged(Core lc, ProxyConfig cfg, RegistrationState cstate, String message) {
            super.onRegistrationStateChanged(lc, cfg, cstate, message);
            Log.d("linphone_注册状态=", cstate.toString() + "===" + message);
            //注册成功
            SipUtils.getIns().setRegister(cstate == RegistrationState.Ok);
            binding.setRegisterState("注册状态"+message);
        }
    };
}
