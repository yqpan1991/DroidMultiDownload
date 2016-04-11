package com.dewmobile.downloaddemo.biz;

import android.content.Context;
import android.text.TextUtils;

import com.dewmobile.downloaddemo.MyApplication;

import java.io.Serializable;

/**
 * Created by panyongqiang on 16/3/31.
 */
public class FileInfo implements Serializable{

    public String url;
    public String localPath;
    public long fileSize;//文件大小

    public void generateLocalPath(Context context){
        if(TextUtils.isEmpty(localPath)){
            localPath = context.getCacheDir()+"/"+ url.substring(url.lastIndexOf("/")+1);
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
