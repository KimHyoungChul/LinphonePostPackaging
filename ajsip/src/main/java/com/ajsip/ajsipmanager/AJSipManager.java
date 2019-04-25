package com.ajsip.ajsipmanager;

/**
 * sip管理类
 */
public class AJSipManager {
    private static volatile AJSipManager ajSipManager;
    private static AJSipAccount ajSipAccount;
    private static AJSipCall ajSipCall;
    public AJSipManager() {
        ajSipAccount=new AJSipAccount();
        ajSipCall=new AJSipCall();
    }

    public static AJSipManager getIns(){
        if (ajSipManager==null){
            ajSipManager=new AJSipManager();
        }
        return ajSipManager;
    }
    public static AJSipAccount getAccount(){
        if (ajSipManager==null){
            getIns();
        }
        return ajSipAccount;
    }
    public static AJSipCall getCall(){
        if (ajSipCall==null){
            getIns();
        }
        return ajSipCall;
    }

    public static void stop(){
        if (ajSipCall!=null){
            ajSipCall.hangUp();
        }
        if (ajSipAccount!=null){
            ajSipAccount.signOut();
            ajSipAccount=null;
        }
        if (ajSipManager!=null){
            ajSipManager=null;
        }

    }

}
