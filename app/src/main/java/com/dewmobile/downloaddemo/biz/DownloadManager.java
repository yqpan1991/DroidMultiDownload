package com.dewmobile.downloaddemo.biz;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.dewmobile.downloaddemo.MyApplication;
import com.dewmobile.downloaddemo.biz.db.DownloadBean;
import com.dewmobile.downloaddemo.biz.db.DownloadDatabaseHelper;
import com.dewmobile.downloaddemo.biz.db.DownloadInfo;
import com.dewmobile.downloaddemo.biz.db.DownloadTask;

/**
 * Created by panyongqiang on 16/3/31.
 */
public class DownloadManager {

    private static DownloadManager mInstance;
    private Context mContext;
    private ThreadPoolManager threadPoolManager;
    private DownloadBroadHelper downloadBroadHelper;
    private DownloadTaskManager mDownloadTaskManager;

    private DownloadManager(Context context){
        mContext = context;
        threadPoolManager = ThreadPoolManager.getInstance();
        downloadBroadHelper = new DownloadBroadHelper(mContext);
        mDownloadTaskManager = new DownloadTaskManager(mDownloadCallback);
    }

    public static DownloadManager getInstance(){
        if(mInstance == null){
            synchronized (DownloadManager.class){
                if(mInstance == null){
                    mInstance = new DownloadManager(MyApplication.getContext());
                }
            }
        }
        return mInstance;
    }

    public void downloadByNormal(String url){
        if(TextUtils.isEmpty(url)){
            return;
        }
        threadPoolManager.getPreDownloadThreadPool().execute(buildDownloadNormal(url));
    }

    private Runnable buildDownloadNormal(String url){
        FileInfo fileInfo = new FileInfo();
        fileInfo.url = url;
        Runnable runnable = new InitDownloadInfoTask(fileInfo, new InitDownloadInfoTask.InitCallback(){

            @Override
            public void onNewDownloadRecord(DownloadBean bean) {
                mDownloadTaskManager.addDownload(new DownloadInfo(bean));
            }

            @Override
            public void onDownloadRecordExist(DownloadBean bean) {
                if(!bean.isDownloadSucceed()){
                    mDownloadTaskManager.addDownload(new DownloadInfo(bean));
                }else{
                    DownloadManager.getInstance().getDownloadBroadHelper().notifyDownloadStatusChanged(new DownloadInfo(bean), DownloadStatus.SUCCESS);
                }
            }

            @Override
            public void onInitError(FileInfo fileInfo) {
                //notify error
            }
        });
        return runnable;
    }

    public void pause(final DownloadInfo downloadInfo){
        threadPoolManager.getPreDownloadThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                mDownloadTaskManager.pauseDownload(downloadInfo);
            }
        });
    }

    public DownloadBroadHelper getDownloadBroadHelper(){
        return downloadBroadHelper;
    }


    private DownloadTask.DownloadCallback mDownloadCallback = new DownloadTask.DownloadCallback() {
        @Override
        public void onDownloadStart(DownloadTask downloadTask, DownloadInfo downloadInfo) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DownloadDatabaseHelper.COLUMN_STATUS, DownloadDatabaseHelper.STATUS_DOWNLOADING);
            DownloadDatabaseHelper.getInstance().updateValues(downloadInfo.id, contentValues);
        }

        @Override
        public void onDownloadProgressUpdated(DownloadTask downloadTask, DownloadInfo downloadInfo) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DownloadDatabaseHelper.COLUMN_CURRENT_SIZE, downloadInfo.currentSize);
            contentValues.put(DownloadDatabaseHelper.COLUMN_TOTAL_SIZE, downloadInfo.totalSize);
            DownloadDatabaseHelper.getInstance().updateValues(downloadInfo.id, contentValues);
        }

        @Override
        public void onDownloadPaused(DownloadTask downloadTask, DownloadInfo downloadInfo) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DownloadDatabaseHelper.COLUMN_STATUS, DownloadDatabaseHelper.STATUS_PAUSE);
            DownloadDatabaseHelper.getInstance().updateValues(downloadInfo.id, contentValues);
        }

        @Override
        public void onDownloadFailed(DownloadTask downloadTask, DownloadInfo downloadInfo) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DownloadDatabaseHelper.COLUMN_STATUS, DownloadDatabaseHelper.STATUS_NET_ERROR);
            DownloadDatabaseHelper.getInstance().updateValues(downloadInfo.id, contentValues);
        }

        @Override
        public void onDownloadSucceed(DownloadTask downloadTask, DownloadInfo downloadInfo) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DownloadDatabaseHelper.COLUMN_STATUS, DownloadDatabaseHelper.STATUS_FINISHED);
            DownloadDatabaseHelper.getInstance().updateValues(downloadInfo.id, contentValues);
        }

        @Override
        public void onDownloadCanceled(DownloadTask downloadTask, DownloadInfo downloadInfo) {
            DownloadDatabaseHelper.getInstance().deleteDownload(downloadInfo.id);
        }
    };

    public enum DownloadStatus{
        INIT,//
        INIT_NORMAL_ERROR,//can
        INIT_FETAL_ERROR,
        INIT_PAUSE,
        WAITING,//start download
        DOWNLOADING,//downloading
        PAUSE,//pause
        ERROR,//download error
        SUCCESS,//下载成功
    }


}
