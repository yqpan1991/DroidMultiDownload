package com.dewmobile.downloaddemo.biz.db;

import android.util.Log;

import com.dewmobile.downloaddemo.biz.DownloadManager;

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
 */
public class DownloadTask implements Runnable {

    private DownloadInfo downloadInfo;

    private final String TAG = this.getClass().getSimpleName();

    private DownloadManager.DownloadStatus downloadStatus;
    long lastReportTime = System.currentTimeMillis();
    private long startTime;

    private DownloadCallback mDownloadCallback;
    private int command;

    private static final int CMD_START = 0;
    private static final int CMD_PAUSE = 1;
    private static final int CMD_DELETE = 2;

    public void pause(){
        command = CMD_PAUSE;
    }

    public void delete(){
        command = CMD_DELETE;
    }

    public boolean canDownload(){
        return command == CMD_START;
    }

    public DownloadTask(DownloadInfo downloadInfo, DownloadCallback downloadCallback) {
        this.downloadInfo = downloadInfo;
        downloadStatus = DownloadManager.DownloadStatus.WAITING;
        mDownloadCallback = downloadCallback;
        command = CMD_START;
    }

    @Override
    public void run() {
        if(!canDownload()){
            notifyCommandStatusIfNotRun();
            return;
        }

        startTime = System.currentTimeMillis();

        File file = checkGenerateFile();
        if (file == null) {
            downloadStatus = DownloadManager.DownloadStatus.ERROR;
            notifyStatusChanged();
            if(mDownloadCallback != null){
                mDownloadCallback.onDownloadFailed(this, downloadInfo);
            }
            return;
        }
        downloadStatus = DownloadManager.DownloadStatus.DOWNLOADING;
        notifyStatusChanged();
        if(mDownloadCallback != null){
            mDownloadCallback.onDownloadStart(this, downloadInfo);
        }
        new DownloadThread().run();
    }

    private void notifyCommandStatusIfNotRun() {
        if(command == CMD_DELETE){
            if(mDownloadCallback != null){
                Log.e(TAG, "onDownloadCanceled");
                mDownloadCallback.onDownloadCanceled(this, downloadInfo);
            }
        }else if(command == CMD_PAUSE){
            if(mDownloadCallback != null){
                Log.e(TAG, "onDownloadPaused");
                mDownloadCallback.onDownloadPaused(this, downloadInfo);
            }
        }
    }

    public DownloadInfo getDownloadInfo(){
        return downloadInfo;
    }

    private File checkGenerateFile() {
        File file = new File(downloadInfo.localPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }


    protected void notifyStatusChanged() {
        DownloadManager.getInstance().getDownloadBroadHelper().notifyDownloadStatusChanged(downloadInfo, downloadStatus);
    }

    protected void notifyProgressChanged(long currentSize, long totalSize) {
        DownloadManager.getInstance().getDownloadBroadHelper().notifyProgressChanged(downloadInfo, currentSize, totalSize);
    }


    public class DownloadThread implements Runnable {

        public DownloadThread() {

        }

        @Override
        public void run() {
            URL url = null;
            try {
                url = new URL(downloadInfo.downloadUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-type","application/octet-stream");
                if (downloadInfo.currentSize != 0) {
                    connection.setRequestProperty("RANGE", "bytes=" + downloadInfo.currentSize + "-" + downloadInfo.totalSize);
                }
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL || connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    downloadInfo.totalSize = downloadInfo.currentSize+connection.getContentLength();
                    InputStream inputStream = connection.getInputStream();
                    bis = new BufferedInputStream(inputStream, 1024*32);
                    bos = new BufferedOutputStream(new FileOutputStream(new File(downloadInfo.localPath)), 1024*32);
                    int length = 0;
                    long tempBufferedSize = 0;
                    long startTime = System.currentTimeMillis();
                    byte[] buffer = new byte[1024 * 32];
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
                    downloadStatus = DownloadManager.DownloadStatus.ERROR;
                    notifyStatusChanged();
                    if(mDownloadCallback != null){
                        mDownloadCallback.onDownloadFailed(DownloadTask.this, downloadInfo);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if(mDownloadCallback != null){
                    mDownloadCallback.onDownloadFailed(DownloadTask.this, downloadInfo);
                }
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                    }
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(!canDownload()){
                notifyCommandStatusIfNotRun();
            }else{
                checkFinish();
            }
        }
    }

    private void checkFinish() {
        if (downloadInfo.currentSize == downloadInfo.totalSize && downloadInfo.currentSize != 0) {
            Log.e(TAG, "consume time:" + (System.currentTimeMillis() - startTime));
            downloadStatus = DownloadManager.DownloadStatus.SUCCESS;
            notifyStatusChanged();
            if(mDownloadCallback != null){
                mDownloadCallback.onDownloadSucceed(this, downloadInfo);
            }
        } else {
            Log.e(TAG, "finish:" + downloadInfo.currentSize + "," + downloadInfo.totalSize);
            if(mDownloadCallback != null){
                mDownloadCallback.onDownloadFailed(this, downloadInfo);
            }
        }
    }

    private void addCompleteSize(long size) {
        Log.e(TAG,"download size:"+size);
        if(size > 0){
            downloadInfo.currentSize += size;
            if (System.currentTimeMillis() - lastReportTime > 1000) {
                lastReportTime = System.currentTimeMillis();
                notifyProgressChanged(downloadInfo.currentSize, downloadInfo.totalSize);
                if (mDownloadCallback != null) {
                    mDownloadCallback.onDownloadProgressUpdated(this, downloadInfo);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof DownloadTask){
            DownloadTask compare = (DownloadTask) o;
            return compare.downloadInfo.equals(downloadInfo) && compare.command == command;
        }else{
            return super.equals(o);
        }
    }

    //下载开始，下载更新，下载暂停，下载失败，下载成功, 继续
    public interface DownloadCallback{
        void onDownloadStart(DownloadTask downloadTask, DownloadInfo downloadInfo);
        void onDownloadProgressUpdated(DownloadTask downloadTask, DownloadInfo downloadInfo);
        void onDownloadPaused(DownloadTask downloadTask, DownloadInfo downloadInfo);
        void onDownloadFailed(DownloadTask downloadTask, DownloadInfo downloadInfo);
        void onDownloadSucceed(DownloadTask downloadTask, DownloadInfo downloadInfo);
        void onDownloadCanceled(DownloadTask downloadTask, DownloadInfo downloadInfo);
    }

}
