package com.dewmobile.downloaddemo.biz.db;

import android.database.Cursor;
import android.text.TextUtils;

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

    public DownloadBean(Cursor cursor, DownloadDatabaseHelper.DownloadColumnIndex columnIndex){
        if(cursor != null){
            id = cursor.getLong(columnIndex.indexId);
            downloadUrl = cursor.getString(columnIndex.indexUrl);
            localPath = cursor.getString(columnIndex.indexLocalPath);
            netType = cursor.getInt(columnIndex.indexNetType);
            status = cursor.getInt(columnIndex.indexStatus);
            totalSize = cursor.getLong(columnIndex.indexTotalSize);
            currentSize = cursor.getLong(columnIndex.indexCurrentSize);
        }
    }

    public DownloadBean(){

    }

    public DownloadBean(FileInfo fileInfo){
        downloadUrl = fileInfo.url;
        localPath = fileInfo.localPath;
        totalSize = fileInfo.fileSize;
    }


    @Override
    public int hashCode() {
        if(TextUtils.isEmpty(downloadUrl)){
            return downloadUrl.hashCode();
        }else{
            return super.hashCode();
        }
    }

    public boolean isDownloadSucceed(){
        return status == DownloadDatabaseHelper.STATUS_FINISHED;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof DownloadBean){
            DownloadBean compare = (DownloadBean) o;
            if(!TextUtils.isEmpty(compare.downloadUrl) && !TextUtils.isEmpty(downloadUrl)){
                return downloadUrl.equalsIgnoreCase(compare.downloadUrl);
            }
        }
        return super.equals(o);
    }
}
