package com.dewmobile.downloaddemo.biz.cacher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;

import com.dewmobile.downloaddemo.biz.DownloadBroadHelper;
import com.dewmobile.downloaddemo.biz.db.DownloadBean;
import com.dewmobile.downloaddemo.biz.db.DownloadDatabaseHelper;
import com.dewmobile.downloaddemo.biz.db.DownloadInfo;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 1. 注册当前url的监听,如果没有的话,那么向数据库查询信息,然后存储到内存缓存中
 * 2. 在当前url的状态发生更改时,向外通知
 * Created by Panda on 2016/4/20.
 */
public class DownloadUICacher {
    private Context mContext;
    private HashMap<String, DownloadStatusInfo> mStatusCacherMap;
    private HashMap<String, DownloadStatusInfo> mNotCacherMap;
    private OnDownloadStatusChangedListener mListener;
    private HandlerThread mWorkHandlerThread;
    private Handler mWorkHandler;
    private Handler mUiHandler;
    private DownloadDatabaseHelper mDownloadDbHelper;


    public DownloadUICacher(Context context, OnDownloadStatusChangedListener listener) {
        mContext = context;
        mListener = listener;
        mStatusCacherMap = new HashMap<>();
        mNotCacherMap = new HashMap<>();
        registerObserver();
        mDownloadDbHelper = DownloadDatabaseHelper.getInstance();
    }

    private void registerObserver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadBroadHelper.ACTION_DOWNLOAD_PROGRESS);
        filter.addAction(DownloadBroadHelper.ACTION_DOWNLOAD_STATUS);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mDownloadReceiver, filter);
    }


    public DownloadStatusInfo getDownloadStatusInfo(final String url, final View attachView, final Object attachObject) {
        //TODO --------------------
        //if exist,  update attachView, then return
        //else check from db, if not exist, return null,存在逻辑漏洞,因为可能有的就是不在数据库中,那么这个状态,应该如何表示?????????????
        //1. 查询数据库,有两种结果
        //2. 1) 数据库中存在这个数据.将其状态放置到下载中的列表中
        //3. 2) 数据库中不存在这个数据,将其放置到未下载的列表中
        //4. 如果列表滑动频繁的话,可能会存在多次查询相同的列表
        //TODO -------. 因而可以设置查询中的一个url列表,防止多次查询,这个是后话,因为会存在一个同步的问题
        //
        if(TextUtils.isEmpty(url)){
            return null;
        }
        if(containsInCache(url)){
            if(mStatusCacherMap.containsKey(url)){
                DownloadStatusInfo downloadStatusInfo = mStatusCacherMap.get(url);
                downloadStatusInfo.attachObject = attachObject;
                downloadStatusInfo.setView(attachView);
                return downloadStatusInfo;
            }
            if(mNotCacherMap.containsKey(url)){
                DownloadStatusInfo downloadStatusInfo = mNotCacherMap.get(url);
                downloadStatusInfo.attachObject = attachObject;
                downloadStatusInfo.setView(attachView);
                return downloadStatusInfo;
            }
        }
        //采用handlerThread的方式,来查询这个信息即可
        checkStartThread();
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                final DownloadBean downloadBean = mDownloadDbHelper.queryDownloadBean(url);
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(downloadBean != null){
                            DownloadStatusInfo info = new DownloadStatusInfo();
                            info.downloadInfo = new DownloadInfo(downloadBean);
                            info.setView(attachView);
                            info.attachObject = attachObject;
                            mStatusCacherMap.put(url,info);
                            notifyDownloadStatusChanged(info);
                        }else{
                            DownloadStatusInfo info = new DownloadStatusInfo();
                            info.setView(attachView);
                            info.attachObject = attachObject;
                            mNotCacherMap.put(url,info);
                        }
                    }
                });
            }
        });

        return null;
    }

    private boolean containsInCache(String url) {
        return mStatusCacherMap.containsKey(url) || mNotCacherMap.containsKey(url);
    }

    private void checkStartThread() {
        if(mWorkHandlerThread == null){
            mWorkHandlerThread = new HandlerThread("download-ui-cacher");
            mWorkHandlerThread.start();
            mWorkHandler = new Handler(mWorkHandlerThread.getLooper());
            mUiHandler = new Handler();
        }
    }


    public class DownloadStatusInfo {
        private WeakReference<View> mViewRef;
        public DownloadInfo downloadInfo;
        public Object attachObject;
        public void setView(View view){
            mViewRef = new WeakReference<>(view);
        }
        public View getView(){
            if(mViewRef != null){
                return mViewRef.get();
            }
            return null;
        }
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
            }else if(mNotCacherMap.containsKey(downloadInfo.downloadUrl)){
                //从未缓存中移除
                //加入到缓存中
                DownloadStatusInfo downloadStatusInfo = mNotCacherMap.get(downloadInfo.downloadUrl);
                mNotCacherMap.remove(downloadInfo.downloadUrl);
                downloadStatusInfo.downloadInfo = downloadInfo;
                mStatusCacherMap.put(downloadInfo.downloadUrl, downloadStatusInfo);
                notifyDownloadStatusChanged(downloadStatusInfo);
            }
        }
    };

    private void notifyDownloadStatusChanged(DownloadStatusInfo info) {
        if (mListener != null) {
            mListener.onDownloadStatusChanged(info.downloadInfo.downloadUrl, info.downloadInfo, info.getView(), info.attachObject);
        }
    }

    public interface OnDownloadStatusChangedListener {
        void onDownloadStatusChanged(String url, DownloadInfo downloadInfo, View attachView, Object attachObject);
    }

    public void destroy() {
        checkDestroyThread();
        //remove listener
        //remove broadcast receiver
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDownloadReceiver);

    }

    private void checkDestroyThread() {
        if(mWorkHandlerThread != null){
            mWorkHandler.removeCallbacksAndMessages(null);
            mWorkHandlerThread.quit();
            mUiHandler.removeCallbacksAndMessages(null);
        }
    }
}
