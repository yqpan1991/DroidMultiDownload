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

    public void download(String url){
        if(TextUtils.isEmpty(url)){
            return;
        }
        threadPoolManager.getDownloadThreadPool().execute(buildDownload(url));
/*        //根据url，插入到数据库中即可
        //数据库在插入成功后，向外通知插入数据库成功
        //然后开始生成线程，下载文件的概要信息
        //localpath
        //fileSize等等信息
        //下次再次进入时，读取数据库的状态即可

        //文件的概要信息下载完成后
        //生成下载的task，向外通知即可
        //根据下载的url去查询即可
        FileInfo info = new FileInfo();
        info.url = url;
        info.localPath = mContext.getCacheDir()+"/"+ "123.zip";
        DownloadTask4 task = new DownloadTask4();
        task.downloadInfo = info;
        task.callback = callback;
        new Thread(task).start();*/
    }

    public void downloadByNormal(String url){
        if(TextUtils.isEmpty(url)){
            return;
        }
        threadPoolManager.getPreDownloadThreadPool().execute(buildDownloadNormal(url));
    }



    private Runnable buildDownload(String url){
        Runnable runnable =  new InitDownloadInfoTask(mContext, url, new InitDownloadInfoTask.InitCallback() {
            @Override
            public void initSucceed(FileInfo fileInfo) {
                DownloadTask2 downloadTask = new DownloadTask2();
                downloadTask.downloadInfo = fileInfo;
                downloadTask.run();
            }

            @Override
            public void initFailed() {

            }
        });
        return runnable;
    }

    private Runnable buildDownloadNormal(String url){
        FileInfo fileInfo = new FileInfo();
        fileInfo.url = url;
        Runnable runnable = new InitDownloadInfoTask2(fileInfo, new InitDownloadInfoTask2.InitCallback(){

            @Override
            public void onNewDownloadRecord(DownloadBean bean) {
                mDownloadTaskManager.addDownload(new DownloadInfo(bean));
            }

            @Override
            public void onDownloadRecordExist(DownloadBean bean) {
                if(!bean.isDownloadSucceed()){
                    mDownloadTaskManager.addDownload(new DownloadInfo(bean));
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
