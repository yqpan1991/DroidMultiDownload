package com.dewmobile.downloaddemo.biz;

import android.content.Context;
import android.text.TextUtils;

import com.dewmobile.downloaddemo.MyApplication;
import com.edus.utils.StorageHelper;

import java.io.File;
import java.io.Serializable;

/**
 * Created by panyongqiang on 16/3/31.
 */
public class FileInfo implements Serializable {

    public String url;
    public String localPath;
    public long fileSize;//文件大小

    public void generateLocalPath() {
        if (TextUtils.isEmpty(localPath)) {
            localPath = StorageHelper.getInstance().getDownloadPath() + File.separator + url.substring(url.lastIndexOf("/") + 1);
        }
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "url='" + url + '\'' +
                ", localPath='" + localPath + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}
