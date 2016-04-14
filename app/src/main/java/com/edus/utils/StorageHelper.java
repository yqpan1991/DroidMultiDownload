package com.edus.utils;

import android.content.Context;
import android.os.Environment;

import com.dewmobile.downloaddemo.MyApplication;

import java.io.File;
import java.util.List;

/**
 * Created by panyongqiang on 16/4/14.
 */
public class StorageHelper {
    private final String TAG = this.getClass().getSimpleName();

    private StorageVolumeHelper storagePathHelper;
    private Context mContext;
    private String primaryPath;
    private String secondPath;
    private String FILE_SPLITTER = File.separator;
    private String downloadPath;
    private String HOME = "edus";
    private String DOWNLOAD = "download";
    private String homePath;



    private static StorageHelper mInstance;
    private List<StorageVolumeHelper.MyStorageVolume> volumePaths;


    public static StorageHelper getInstance(){
        if(mInstance == null){
            synchronized (StorageHelper.class){
                if(mInstance == null){
                    mInstance = new StorageHelper(MyApplication.getContext());
                }
            }
        }
        return mInstance;
    }

    private StorageHelper(Context context){
        storagePathHelper = new StorageVolumeHelper(context);
        init();
    }

    private void init() {
        volumePaths = storagePathHelper.getVolumePaths(true);
//        Log.e(TAG,volumePaths.toString());
        File externalFilePath = Environment.getExternalStorageDirectory();
        primaryPath = externalFilePath.getPath();
//        Log.e(TAG, externalFilePath.getAbsolutePath());
        homePath = primaryPath+ FILE_SPLITTER + HOME;
        initPath(homePath);
        downloadPath = homePath + FILE_SPLITTER+ DOWNLOAD;
        initPath(downloadPath);
    }

    private void initPath(String path) {
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
    }


    public String getPrimaryPath(){
        return primaryPath;
    }

    public String getDownloadPath(){
        return downloadPath;
    }

}
