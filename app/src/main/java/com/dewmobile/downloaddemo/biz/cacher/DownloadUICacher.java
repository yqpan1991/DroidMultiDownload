package com.dewmobile.downloaddemo.biz.cacher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.dewmobile.downloaddemo.biz.DownloadBroadHelper;
import com.dewmobile.downloaddemo.biz.db.DownloadInfo;

import java.util.HashMap;

/**
 * 1. 注册当前url的监听,如果没有的话,那么向数据库查询信息,然后存储到内存缓存中
 * 2. 在当前url的状态发生更改时,向外通知
 * Created by Panda on 2016/4/20.
 */
public class DownloadUICacher {
    private Context mContext;
    private HashMap<String, DownloadStatusInfo> mStatusCacherMap;
    private OnDownloadStatusChangedListener mListener;


    public DownloadUICacher(Context context, OnDownloadStatusChangedListener listener) {
        mContext = context;
        mListener = listener;
        mStatusCacherMap = new HashMap<>();
        registerObserver();
    }

    private void registerObserver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadBroadHelper.ACTION_DOWNLOAD_PROGRESS);
        filter.addAction(DownloadBroadHelper.ACTION_DOWNLOAD_STATUS);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mDownloadReceiver, filter);
    }


    public DownloadStatusInfo getDownloadStatusInfo(String url, View attachView, Object attachObject) {
        //TODO --------------------
        //if exist,  update attachView, then return
        //else check from db, if not exist, return null,存在逻辑漏洞,因为可能有的就是不在数据库中,那么这个状态,应该如何表示?????????????
        return null;
    }


    public class DownloadStatusInfo {
        View view;
        DownloadInfo downloadInfo;
        Object attachObject;
    }

    private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DownloadBroadHelper.ACTION_DOWNLOAD_PROGRESS)) {
                DownloadInfo downloadInfo = (DownloadInfo) intent.getSerializableExtra(DownloadBroadHelper.EXTRA_FILE);
                checkUpdateDownloadInfo(downloadInfo);
            } else if (action.equals(DownloadBroadHelper.ACTION_DOWNLOAD_STATUS)) {
                DownloadInfo downloadInfo = (DownloadInfo) intent.getSerializableExtra(DownloadBroadHelper.EXTRA_FILE);
                checkUpdateDownloadInfo(downloadInfo);
            }
        }

        private void checkUpdateDownloadInfo(DownloadInfo downloadInfo) {
            if (mStatusCacherMap.containsKey(downloadInfo.downloadUrl)) {
                DownloadStatusInfo downloadStatusInfo = mStatusCacherMap.get(downloadInfo.downloadUrl);
                downloadStatusInfo.downloadInfo = downloadInfo;
                notifyDownloadStatusChanged(downloadStatusInfo);
            }
        }
    };

    private void notifyDownloadStatusChanged(DownloadStatusInfo info) {
        if (mListener != null) {
            mListener.onDownloadStatusChanged(info.downloadInfo.downloadUrl, info.downloadInfo, info.view, info.attachObject);
        }
    }

    public interface OnDownloadStatusChangedListener {
        void onDownloadStatusChanged(String url, DownloadInfo downloadInfo, View attachView, Object attachObject);
    }

    public void destroy() {
        //remove listener
        //remove broadcast receiver
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDownloadReceiver);
    }
}
