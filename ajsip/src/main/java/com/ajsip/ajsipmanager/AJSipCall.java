package com.ajsip.ajsipmanager;

import android.content.ContentResolver;
import android.content.Context;

import com.ajsip.LinphoneManager;

import java.util.Map;

public class AJSipCall {


    /**
     * 拨打电话
     *
     * @param context
     * @param number
     */
    public void call(String number, String displayName, Map<String,String> headMap, Context context){

        LinphoneManager.getInstance().call(number,displayName,headMap,context);
    }


    /**
     * 接听电话
     *
     * @return
     */
    public void answer(Context context){
        LinphoneManager.getInstance().answer(context);
    }


    /**
     * 切换扬声器
     *
     * @param isSpeakers
     */
    public void switchingSpeakers(boolean isSpeakers) {
        if (LinphoneManager.getInstance() != null) {
            if (isSpeakers) {
                LinphoneManager.getInstance().routeAudioToSpeaker();
            } else {
                LinphoneManager.getInstance().routeAudioToReceiver();
            }
        } else {
            throw new NullPointerException("LinphoneManager为初始化");
        }

    }

    /**
     * 挂断电话
     */
    public void hangUp() {
        LinphoneManager.getInstance().hangUp();
    }

    /**
     * 通话是否静音
     *
     * @param isMicMuted
     */
    public void switchingMute(boolean isMicMuted) {
        LinphoneManager.getInstance().switchingMute(isMicMuted);
    }

    /**
     * 播放按键声音
     *
     * @param r
     * @param dtmf
     */
    public void playDtmf(ContentResolver r, char dtmf) {
        LinphoneManager.getInstance().playDtmf(r, dtmf);
    }

    /**
     * 关闭按键声音
     */
    public void stopDtmf() {
        LinphoneManager.getInstance().stopDtmf();
    }

    /**
     * 发送按键值
     *
     * @param c
     */
    public void sendDtmf(char c) {
        LinphoneManager.getInstance().sendDtmf(c);
    }

    public String getCurrentDisplayname() {
       return LinphoneManager.getInstance().getAddressDisplayName();
    }

    public String getCallUserName() {
        return LinphoneManager.getInstance().getCallUserName();
    }

    public String getCallDomain() {
        return LinphoneManager.getInstance().getCallDomain();
    }

    public String getasStringUrl() {
        return LinphoneManager.getInstance().getasStringUriOnly();
    }

    public int getDirection() {
        return LinphoneManager.getInstance().getCurrentDirection();
    }
}