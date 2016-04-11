package com.dewmobile.downloaddemo.biz;

import android.util.Log;

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
public class DownloadTask1 implements Runnable {

    public FileInfo downloadInfo;

    private int downloadThreadCount = 3;

    private final String TAG = this.getClass().getSimpleName();

    private DownloadManager.DownloadStatus downloadStatus;
    private long completeSize;
    long lastReportTime = System.currentTimeMillis();
    private long startTime;


    public DownloadTask1() {
        downloadStatus = DownloadManager.DownloadStatus.WAITING;
        completeSize = 0;
    }

    @Override
    public void run() {

        startTime = System.currentTimeMillis();
        File file = new File(downloadInfo.localPath);
        if (file.exists()) {
            file.delete();
        }
        downloadStatus = DownloadManager.DownloadStatus.DOWNLOADING;
        notifyStatusChanged();
        ThreadPoolManager.getInstance().getDownloadThreadPool().execute(new DownloadThread(0, downloadInfo.fileSize));
    }


    protected void notifyStatusChanged() {
//        DownloadManager.getInstance().getDownloadBroadHelper().notifyDownloadStatusChanged(downloadInfo, downloadStatus);
    }

    protected void notifyProgressChanged(long currentSize, long totalSize) {
//        DownloadManager.getInstance().getDownloadBroadHelper().notifyProgressChanged(downloadInfo, currentSize, totalSize);
    }

    public class DownloadThread implements Runnable {
        private long mStartIndex;
        private long mEndIndex;

        public DownloadThread(long startIndex, long endIndex) {
            mStartIndex = startIndex;
            mEndIndex = endIndex;
        }

        @Override
        public void run() {
            URL url = null;
            try {
                url = new URL(downloadInfo.url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedInputStream bis = null;
            File file = null;
            BufferedOutputStream bos = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("RANGE", "bytes=" + mStartIndex + "-" + mEndIndex);
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL || connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    bis = new BufferedInputStream(inputStream, 1024*32);
//                    file = new File(downloadInfo.localPath);
//                    bos = new BufferedOutputStream(new FileOutputStream(file));
                    int length = 0;
                    long tempBufferedSize = 0;
                    long startTime = System.currentTimeMillis();
                    byte[] buffer = new byte[1024 * 32];
                    while ((length = bis.read(buffer, 0, buffer.length)) != -1) {
//                        bos.write(buffer, 0, length);
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
                if(bos != null){
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
        if (completeSize == downloadInfo.fileSize && completeSize != 0) {
            Log.e(TAG, "consume time:" + (System.currentTimeMillis() - startTime));
            downloadStatus = DownloadManager.DownloadStatus.SUCCESS;
            notifyStatusChanged();
        } else {
            Log.e(TAG, "finish:" + completeSize + "," + downloadInfo.fileSize);
        }
    }

    private void addCompleteSize(long size) {
        completeSize += size;
        notifyProgressChanged(completeSize, downloadInfo.fileSize);
        if (System.currentTimeMillis() - lastReportTime > 1000) {
            lastReportTime = System.currentTimeMillis();
            notifyProgressChanged(completeSize, downloadInfo.fileSize);
        }
    }

}
