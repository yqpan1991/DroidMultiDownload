package com.dewmobile.downloaddemo.biz.db;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.dewmobile.downloaddemo.MyApplication;
import com.dewmobile.downloaddemo.biz.DownloadManager;
import com.edus.utils.UrlUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by panyongqiang on 16/3/31.
 * 执行当前的任务时,使用wake_lock
 * 任务结束时,释放wake_lock
 */
public class DownloadTask implements Runnable {

    private static final String WAKE_LOCK_TAG = "DownloadTask";

    private DownloadInfo downloadInfo;

    private final String TAG = this.getClass().getSimpleName();

    private static final int BUFFER_SIZE = 1024 * 32;

    private int downloadStatus;
    long lastReportTime = System.currentTimeMillis();
    private long startTime;

    private DownloadCallback mDownloadCallback;
    private int command;

    private static final String TEMP_SUFFIX = ".es";
    private String tempPath;
    private PowerManager.WakeLock mWakeLock;

    private static final int CMD_START = 0;
    private static final int CMD_PAUSE = 1;
    private static final int CMD_DELETE = 2;

    public void pause() {
        command = CMD_PAUSE;
    }

    public void delete() {
        command = CMD_DELETE;
    }

    public boolean canDownload() {
        return command == CMD_START;
    }

    public DownloadTask(DownloadInfo downloadInfo, DownloadCallback downloadCallback) {
        this.downloadInfo = downloadInfo;
        downloadStatus = DownloadDatabaseHelper.STATUS_PENDING;
        mDownloadCallback = downloadCallback;
        command = CMD_START;
    }

    @Override
    public void run() {
        acquireWakeLock();
        if (!canDownload()) {
            notifyCommandStatusIfNotRun();
            releaseWakeLock();
            return;
        }

        startTime = System.currentTimeMillis();
        tempPath = downloadInfo.localPath + TEMP_SUFFIX;
        File file = checkGenerateFile();
        if (file == null) {
            downloadStatus = DownloadDatabaseHelper.STATUS_NORMAL_ERROR;
            notifyStatusChanged();
            if (mDownloadCallback != null) {
                mDownloadCallback.onDownloadFailed(this, downloadInfo);
            }
            releaseWakeLock();
            return;
        }
        downloadStatus = DownloadDatabaseHelper.STATUS_DOWNLOADING;
        notifyStatusChanged();
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadStart(this, downloadInfo);
        }
        new DownloadThread().run();
    }

    private void notifyCommandStatusIfNotRun() {
        if (command == CMD_DELETE) {
            Log.e(TAG, "onDownloadCanceled");
            downloadStatus = DownloadDatabaseHelper.STATUS_CANCELED;
            notifyStatusChanged();
            if (mDownloadCallback != null) {
                mDownloadCallback.onDownloadCanceled(this, downloadInfo);
            }
        } else if (command == CMD_PAUSE) {
            downloadStatus = DownloadDatabaseHelper.STATUS_PAUSE;
            notifyStatusChanged();
            if (mDownloadCallback != null) {
                Log.e(TAG, "onDownloadPaused");
                mDownloadCallback.onDownloadPaused(this, downloadInfo);
            }
        }
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    private File checkGenerateFile() {
        File file = new File(tempPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                file.isHidden();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }


    protected void notifyStatusChanged() {
        downloadInfo.status = downloadStatus;
        DownloadManager.getInstance().getDownloadBroadHelper().notifyDownloadStatusChanged(downloadInfo);
    }

    protected void notifyProgressChanged(long currentSize, long totalSize) {
        DownloadManager.getInstance().getDownloadBroadHelper().notifyProgressChanged(downloadInfo);
    }


    public class DownloadThread implements Runnable {

        public DownloadThread() {

        }

        @Override
        public void run() {
            URL url = null;
            try {
                url = new URL(UrlUtils.getEncodedUrl(downloadInfo.downloadUrl));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (url == null) {
                setErrorAndNotify();
                return;
            }
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-type", "application/octet-stream");
                if (downloadInfo.currentSize != 0) {
                    connection.setRequestProperty("RANGE", "bytes=" + downloadInfo.currentSize + "-" + downloadInfo.totalSize);
                }
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL || connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    downloadInfo.totalSize = downloadInfo.currentSize + connection.getContentLength();
                    InputStream inputStream = connection.getInputStream();
                    bis = new BufferedInputStream(inputStream, BUFFER_SIZE);
                    bos = new BufferedOutputStream(new FileOutputStream(new File(tempPath), true), BUFFER_SIZE);
                    int length = 0;
                    long tempBufferedSize = 0;
                    long startTime = System.currentTimeMillis();
                    byte[] buffer = new byte[BUFFER_SIZE];
                    while ((length = bis.read(buffer, 0, buffer.length)) != -1 && canDownload()) {
                        bos.write(buffer, 0, length);
                        tempBufferedSize += length;
                        if (System.currentTimeMillis() - startTime > 1000) {
                            addCompleteSize(tempBufferedSize);
                            startTime = System.currentTimeMillis();
                            tempBufferedSize = 0;
                        }
                    }
                    if (tempBufferedSize != 0) {
                        addCompleteSize(tempBufferedSize);
                    }
                } else {
                    setErrorAndNotify();
                }
            } catch (IOException e) {
                setErrorAndNotify();
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                    }
                }
                if (bos != null) {
                    try {
                        bos.flush();
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!canDownload()) {
                notifyCommandStatusIfNotRun();
            } else {
                checkFinish();
            }
            releaseWakeLock();
        }
    }

    private void setErrorAndNotify() {
        downloadStatus = DownloadDatabaseHelper.STATUS_NET_ERROR;
        notifyStatusChanged();
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadFailed(DownloadTask.this, downloadInfo);
        }
    }

    private void checkFinish() {
        if (downloadInfo.currentSize == downloadInfo.totalSize && downloadInfo.currentSize != 0) {
            downloadInfo.status = DownloadDatabaseHelper.STATUS_FINISHED;
            Log.e(TAG, "consume time:" + (System.currentTimeMillis() - startTime));
            //TODO 文件去除ed的后缀
            if (tempPath.endsWith(TEMP_SUFFIX)) {
                new File(tempPath).renameTo(new File(downloadInfo.localPath));

            }
            downloadStatus = DownloadDatabaseHelper.STATUS_FINISHED;
            notifyStatusChanged();
            if (mDownloadCallback != null) {
                mDownloadCallback.onDownloadSucceed(this, downloadInfo);
            }
        } else {
            Log.e(TAG, "finish:" + downloadInfo.currentSize + "," + downloadInfo.totalSize);
            downloadStatus = DownloadDatabaseHelper.STATUS_NORMAL_ERROR;
            notifyStatusChanged();
            if (mDownloadCallback != null) {
                mDownloadCallback.onDownloadFailed(this, downloadInfo);
            }
        }
    }

    private void addCompleteSize(long size) {
        if (size > 0) {
            downloadInfo.currentSize += size;
            lastReportTime = System.currentTimeMillis();
            notifyProgressChanged(downloadInfo.currentSize, downloadInfo.totalSize);
            if (mDownloadCallback != null) {
                mDownloadCallback.onDownloadProgressUpdated(this, downloadInfo);
            }
        }
    }

    private void acquireWakeLock(){
        if(mWakeLock == null){
            PowerManager pm = (PowerManager) MyApplication.getContext().getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock(){
        if(mWakeLock != null){
            mWakeLock.release();
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DownloadTask) {
            DownloadTask compare = (DownloadTask) o;
            return compare.downloadInfo.equals(downloadInfo) && compare.command == command;
        } else {
            return super.equals(o);
        }
    }

    //下载开始，下载更新，下载暂停，下载失败，下载成功, 继续
    public interface DownloadCallback {
        void onDownloadStart(DownloadTask downloadTask, DownloadInfo downloadInfo);

        void onDownloadProgressUpdated(DownloadTask downloadTask, DownloadInfo downloadInfo);

        void onDownloadPaused(DownloadTask downloadTask, DownloadInfo downloadInfo);

        void onDownloadFailed(DownloadTask downloadTask, DownloadInfo downloadInfo);

        void onDownloadSucceed(DownloadTask downloadTask, DownloadInfo downloadInfo);

        void onDownloadCanceled(DownloadTask downloadTask, DownloadInfo downloadInfo);
    }

}
