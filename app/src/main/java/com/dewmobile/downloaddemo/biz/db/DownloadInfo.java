package com.dewmobile.downloaddemo.biz.db;

import android.database.Cursor;
import android.text.TextUtils;

import com.dewmobile.downloaddemo.biz.FileInfo;

import java.io.Serializable;

/**
 * Created by panyongqiang on 16/4/11.
 */
public class DownloadInfo extends DownloadBean implements Serializable{

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

    public DownloadInfo(){

    }



    @Override
    public int hashCode() {
        if(TextUtils.isEmpty(downloadUrl)){
            return downloadUrl.hashCode();
        }else{
            return super.hashCode();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof DownloadInfo){
            DownloadInfo compare = (DownloadInfo) o;
            if(!TextUtils.isEmpty(compare.downloadUrl) && !TextUtils.isEmpty(downloadUrl)){
                return downloadUrl.equalsIgnoreCase(compare.downloadUrl);
            }
        }
        return super.equals(o);
    }
}
