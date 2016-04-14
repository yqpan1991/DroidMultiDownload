package com.dewmobile.downloaddemo;

import android.app.Application;
import android.content.Context;

import com.dewmobile.downloaddemo.biz.ThreadPoolManager;
import com.edus.utils.StorageHelper;

/**
 * Created by panyongqiang on 16/4/8.
 */
public class MyApplication extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        init();
    }

    private void init() {
        ThreadPoolManager.getInstance().getPreDownloadThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                StorageHelper.getInstance();
            }
        });
    }
}
