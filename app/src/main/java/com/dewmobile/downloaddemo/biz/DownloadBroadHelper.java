package com.dewmobile.downloaddemo.biz;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.dewmobile.downloaddemo.biz.db.DownloadInfo;

import java.io.File;

/**
 * Created by panyongqiang on 16/4/8.
 */
public class DownloadBroadHelper {
    private Context mContext;
    private LocalBroadcastManager mLocalBroadcastManager;
    public static String EXTRA_FILE = "extra_file";
    public static String ACTION_DOWNLOAD_PROGRESS = "action_download_proress";
    public static String EXTRA_TOTAL = "extra_total";
    public static String EXTRA_CURRENT = "extra_current";
    public static String ACTION_DOWNLOAD_STATUS = "action_download_status";
    public static String EXTRA_STATUS = "extra_status";

    public DownloadBroadHelper(Context context) {
        mContext = context;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
    }

    public void notifyProgressChanged(DownloadInfo fileInfo, long currentSize, long totalSize) {
        Intent intent = new Intent(ACTION_DOWNLOAD_PROGRESS);
        intent.putExtra(EXTRA_TOTAL, totalSize);
        intent.putExtra(EXTRA_CURRENT, currentSize);
        if (fileInfo != null) {
            intent.putExtra(EXTRA_FILE, fileInfo);
        }
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public void notifyDownloadStatusChanged(DownloadInfo fileInfo, DownloadManager.DownloadStatus downloadStatus) {
        Intent intent = new Intent(ACTION_DOWNLOAD_STATUS);
        intent.putExtra(EXTRA_STATUS, downloadStatus);
        if (fileInfo != null) {
            intent.putExtra(EXTRA_FILE, fileInfo);
        }
        mLocalBroadcastManager.sendBroadcast(intent);
    }
}
