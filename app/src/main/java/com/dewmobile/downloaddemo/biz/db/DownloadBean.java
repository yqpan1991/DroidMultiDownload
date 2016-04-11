package com.dewmobile.downloaddemo.biz.db;

import android.database.Cursor;

import com.dewmobile.downloaddemo.biz.FileInfo;

/**
 * Created by panyongqiang on 16/4/11.
 */
public class DownloadBean {
    public long id;
    public String downloadUrl;
    public String localPath;
    public int netType;
    public int status;
    public long totalSize;
    public long currentSize;

    public DownloadBean(Cursor cursor){

    }

    public DownloadBean(){

    }

    public DownloadBean(FileInfo fileInfo){
        downloadUrl = fileInfo.url;
        localPath = fileInfo.localPath;
        totalSize = fileInfo.fileSize;
    }

    public boolean needStartAgain(){
        return false;
    }
}
