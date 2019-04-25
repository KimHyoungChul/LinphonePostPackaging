package com.example.linphonetest;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.ajsip.LinphoneManager;
import com.ajsip.ajsipmanager.AJSipManager;
import com.ajsip.callback.AJCoreListenerStub;
import com.ajsip.state.AJCallState;
import com.ajsip.state.AJGlobalState;
import com.ajsip.state.AJRegistrationState;
import com.example.linphonetest.databinding.ActivityMainBinding;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String username = "69802008619";
    String password = "101944957262#";
    String displayname = "17778115596";
    String domain = "cxc32.italkbb.com:10000";
    //域名配置详细查看沙方洲发的"用户获取sip号码流程文档"
    private String userAgentName = "softphone_android";
    private String userAgentversion = "4.0.1";

    HashMap<String,String> headMap=new HashMap<>();

    ActivityMainBinding binding;
    private boolean isSpeakerEnabled = false, isMicMuted = false, isPlayKey;

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
        ////头部配置详细查看沙方洲发的"用户获取sip号码流程文档"
        headMap.put("X-ITK-INFO", "con=wi;biz=fc;bid=12345");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linphone_tv1:
                long time1 = System.currentTimeMillis();
                AJSipManager.getAccount().init(getApplicationContext(), userAgentName, userAgentversion, coreListener);
                Log.d("linphone_初始化时间=", System.currentTimeMillis() - time1 + "");
                break;
            case R.id.linphone_tv2:
                String displayName="110";
                AJSipManager.getCall().call("69801006278",displayName,headMap, getApplicationContext());
                break;
            case R.id.linphone_tv3:
                AJSipManager.getCall().hangUp();
                break;
            case R.id.linphone_tv4:
                AJSipManager.getCall().answer(getApplicationContext());
                break;
            case R.id.linphone_tv5:
                isMicMuted = !isMicMuted;
                AJSipManager.getCall().switchingMute(!isMicMuted);
                binding.linphoneTv5.setText("静音" + String.valueOf(isMicMuted));
                break;
            case R.id.linphone_tv6:
                isSpeakerEnabled = !isSpeakerEnabled;
                AJSipManager.getCall().switchingSpeakers(isSpeakerEnabled);
                binding.linphoneTv6.setText("免提" + String.valueOf(this.isSpeakerEnabled));

                break;
            case R.id.linphone_tv7:
                AJSipManager.stop();
                break;
            case R.id.linphone_tv8:
                if (isPlayKey) {
                    AJSipManager.getCall().stopDtmf();
                } else {
                    AJSipManager.getCall().playDtmf(getContentResolver(), '6');
                }
                isPlayKey = !isPlayKey;
                break;
            case R.id.linphone_tv9:
                AJSipManager.getCall().sendDtmf('6');
                break;
            case R.id.linphone_tv10:
                binding.linphoneTv10.setText("10获取通话显示号码"+AJSipManager.getCall().getCurrentDisplayname());
                break;
            case R.id.linphone_tv11:
                binding.linphoneTv11.setText("11获取通话号码"+AJSipManager.getCall().getCallUserName());
                break;
            case R.id.linphone_tv12:
                binding.linphoneTv12.setText("12获取通话地址"+AJSipManager.getCall().getasStringUrl());
                break;
            case R.id.linphone_tv13:
                binding.linphoneTv13.setText("13获取通话时长"+AJSipManager.getCall().getDirection());
                break;
            case R.id.linphone_tv14:
                binding.linphoneTv14.setText("14获取登录账户代理"+AJSipManager.getAccount().getUserAgent());
                break;
            case R.id.linphone_tv15:
                binding.linphoneTv15.setText("15获取登录账户域名"+AJSipManager.getAccount().getDomain(username));
                break;
        }
    }

    AJCoreListenerStub coreListener = new AJCoreListenerStub() {
        @Override
        public void onGlobalStateChanged(AJGlobalState gstate, String message) {
            super.onGlobalStateChanged(gstate, message);
            Log.d("linphone_初始化状态=", gstate.name() + "===" + message);
            binding.setInitState("初始化状态" + message);
            /**
             * 初始化成功
             */
            if (gstate == AJGlobalState.AJGlobal_On) {
                AJSipManager.getAccount().registerSip(username,
                        password,
                        displayname,
                        domain,headMap, getApplicationContext());


            }
        }

        @Override
        public void onRegistrationStateChanged(AJRegistrationState state, String message) {
            super.onRegistrationStateChanged(state, message);
            Log.d("linphone_注册状态=", state.toString() + "===" + message);
            //注册成功
            binding.setRegisterState("注册状态" + message);
        }

        @Override
        public void onCallStateChanged(AJCallState state, String message) {
            super.onCallStateChanged(state, message);
            Log.d("linphone_通话状态=", state.name() + "===" + message);
//            //通话状态
            binding.setCallstate("通话状态" + message);
        }
    };
}
