package com.dewmobile.downloaddemo.biz;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by panyongqiang on 16/4/8.
 */
public class ThreadPoolManager {
    private Executor downloadThreadPool;
    private Executor predownloadThreadPool;

    private static ThreadPoolManager instance;

    public static ThreadPoolManager getInstance(){
        if(instance == null){
            synchronized (ThreadPoolManager.class){
                if(instance == null){
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }

    private ThreadPoolManager(){
        downloadThreadPool = Executors.newFixedThreadPool(10);
        predownloadThreadPool = Executors.newSingleThreadExecutor();
    }
    public Executor getDownloadThreadPool(){
        return downloadThreadPool;
    }

    public Executor getPredownloadThreadPool(){
        return predownloadThreadPool;
    }


}
