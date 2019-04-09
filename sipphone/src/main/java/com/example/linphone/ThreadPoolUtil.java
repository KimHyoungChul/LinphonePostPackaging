package com.example.linphone;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolUtil {

    private volatile static ExecutorService singleExecutor;

    /**
     * 单一子线程（多用于数据库操作）
     * @return
     */
    public static ExecutorService getSingleThread(){
        if (singleExecutor==null){
            synchronized (ThreadPoolUtil.class){
                if (singleExecutor==null){
                    singleExecutor= Executors.newSingleThreadExecutor();
                }
            }
        }
        return singleExecutor;
    }
}
