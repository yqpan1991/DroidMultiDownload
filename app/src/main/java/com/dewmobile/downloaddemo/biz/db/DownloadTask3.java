package com.dewmobile.downloaddemo.biz.db;

import android.util.Log;

import com.dewmobile.downloaddemo.biz.DownloadManager;
import com.dewmobile.downloaddemo.biz.FileInfo;
import com.dewmobile.downloaddemo.biz.ThreadPoolManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by panyongqiang on 16/3/31.
 */
public class DownloadTask3 implements Runnable {

    private DownloadInfo downloadInfo;

    private final String TAG = this.getClass().getSimpleName();

    private DownloadManager.DownloadStatus downloadStatus;
    private long completeSize;
    long lastReportTime = System.currentTimeMillis();
    private long startTime;
    //TODO ------同样需要添加callback，对于不同的状态，需要做处理是不同的
    //下载开始，下载更新，下载失败，下载成功


    public DownloadTask3(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
        downloadStatus = DownloadManager.DownloadStatus.WAITING;
        completeSize = 0;
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        File file = checkGenerateFile();
        if (file == null) {
            downloadStatus = DownloadManager.DownloadStatus.ERROR;
            notifyStatusChanged();
            return;
        }

        downloadStatus = DownloadManager.DownloadStatus.DOWNLOADING;
        notifyStatusChanged();
        ThreadPoolManager.getInstance().getDownloadThreadPool().execute(new DownloadThread());
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
                if (downloadInfo.currentSize != 0) {
                    connection.setRequestProperty("RANGE", "bytes=" + downloadInfo.currentSize + "-" + downloadInfo.totalSize);
                }
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL || connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    downloadInfo.totalSize = downloadInfo.currentSize+connection.getContentLength();
                    InputStream inputStream = connection.getInputStream();
                    bis = new BufferedInputStream(inputStream);
                    bos = new BufferedOutputStream(new FileOutputStream(new File(downloadInfo.localPath)));
                    int length = 0;
                    long tempBufferedSize = 0;
                    long startTime = System.currentTimeMillis();
                    byte[] buffer = new byte[1024 * 8];
                    while ((length = bis.read(buffer, 0, buffer.length)) != -1) {
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
                        tempBufferedSize = 0;
                    }
                } else {
                    downloadStatus = DownloadManager.DownloadStatus.ERROR;
                    notifyStatusChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
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
            checkFinish();
        }
    }

    private void checkFinish() {
        if (completeSize == downloadInfo.totalSize && completeSize != 0) {
            Log.e(TAG, "consume time:" + (System.currentTimeMillis() - startTime));
            downloadStatus = DownloadManager.DownloadStatus.SUCCESS;
            notifyStatusChanged();
        } else {
            Log.e(TAG, "finish:" + completeSize + "," + downloadInfo.totalSize);
        }
    }

    private void addCompleteSize(long size) {
        completeSize += size;
        notifyProgressChanged(completeSize, downloadInfo.totalSize);
        if (System.currentTimeMillis() - lastReportTime > 1000) {
            lastReportTime = System.currentTimeMillis();
            notifyProgressChanged(completeSize, downloadInfo.totalSize);
        }
    }

}
