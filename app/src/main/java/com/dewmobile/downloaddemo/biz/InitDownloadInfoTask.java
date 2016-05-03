package com.dewmobile.downloaddemo.biz;

import android.content.Context;
import android.os.PowerManager;
import android.text.TextUtils;

import com.dewmobile.downloaddemo.MyApplication;
import com.dewmobile.downloaddemo.biz.db.DownloadBean;
import com.dewmobile.downloaddemo.biz.db.DownloadDatabaseHelper;

/**
 * 初始化下载的信息
 * 2. 检测当前的url是否在下载的数据库中，如果在，从数据库获取初始化文件的信息，检查状态位，如果是处于下载的队列中不做处理，否则，更改将其更改为排队下载中
 * 3. 如果没有在下载的记录中，生成完整的文件信息，然后生成一个下载的记录，插入到数据库中
 * 同样插入到数据库中，然后将状态位更改为排队下载中
 * 任务开始时,需要使用wake_lock
 * 任务结束时,释放wake_lock
 */
public class InitDownloadInfoTask implements Runnable{

    private static final String WAKE_LOCK_TAG = "InitDownloadInfoTask";


    private FileInfo mFileInfo;
    //网络信息
    private InitCallback mInitCallback;
    private PowerManager.WakeLock mWakeLock;

    public InitDownloadInfoTask(FileInfo fileInfo, InitCallback initCallback){
        mFileInfo = fileInfo;
        mInitCallback = initCallback;
    }

    @Override
    public void run() {
        acquireWakeLock();
        if(TextUtils.isEmpty(mFileInfo.url)){
            notifyOnInitError(mFileInfo);
            releaseWakeLock();
            return;
        }
        DownloadDatabaseHelper databaseHelper = DownloadDatabaseHelper.getInstance();
        DownloadBean bean = databaseHelper.queryDownloadBean(mFileInfo.url);
        if(bean != null){
            notifyOnDownloadRecordExist(bean);
        }else{
            //TODO ------need to check from db , if current filePath is exist,if exist,then add -1
            mFileInfo.generateLocalPath();
            bean = new DownloadBean(mFileInfo);
            bean.netType = 0;
            bean.status = DownloadDatabaseHelper.STATUS_PENDING;
            bean.currentSize = 0;
            databaseHelper.addDownloadRecord(bean);
            notifyNewDownloadRecord(bean);
        }
        releaseWakeLock();
    }

    private void notifyOnDownloadRecordExist(DownloadBean bean){
        if(mInitCallback != null){
            mInitCallback.onDownloadRecordExist(bean);
        }
    }
    private void notifyOnInitError(FileInfo fileInfo){
        if(mInitCallback != null){
            mInitCallback.onInitError(fileInfo);
        }
    }

    private void notifyNewDownloadRecord(DownloadBean bean){
        if(mInitCallback != null){
            mInitCallback.onNewDownloadRecord(bean);
        }
    }

    public interface InitCallback{
        void onNewDownloadRecord(DownloadBean bean);
        void onDownloadRecordExist(DownloadBean bean);
        void onInitError(FileInfo fileInfo);
    }

    private void acquireWakeLock(){
        PowerManager pm = (PowerManager) MyApplication.getContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        mWakeLock.acquire();
    }

    private void releaseWakeLock(){
        if(mWakeLock != null){
            mWakeLock.release();
        }
    }

}
