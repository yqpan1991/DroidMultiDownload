package com.dewmobile.downloaddemo.biz.db;

import android.database.Cursor;

import com.dewmobile.downloaddemo.biz.FileInfo;

import java.io.Serializable;

/**
 * Created by panyongqiang on 16/4/11.
 */
public class DownloadInfo extends DownloadBean implements Serializable{

    public DownloadInfo(Cursor cursor) {
        super(cursor);
    }

    public DownloadInfo(FileInfo fileInfo) {
        super(fileInfo);
    }

    public DownloadInfo(DownloadBean downloadBean){
        super();
        this.id = downloadBean.id;
        this.downloadUrl = downloadBean.downloadUrl;
        this.localPath = downloadBean.localPath;
        this.netType = downloadBean.netType;
        this.status = downloadBean.status;
        this.totalSize = downloadBean.totalSize;
        this.currentSize = downloadBean.currentSize;
    }
}
