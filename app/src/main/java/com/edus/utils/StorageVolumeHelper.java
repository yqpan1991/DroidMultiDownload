package com.edus.utils;

import android.content.Context;
import android.os.storage.StorageManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by panyongqiang on 16/4/14.
 */
public class StorageVolumeHelper {
    private Context mContext;
    private List<MyStorageVolume> mPathList;

    public StorageVolumeHelper(Context context){
        mContext = context;
        mPathList = new ArrayList<>();
    }

    public List<MyStorageVolume> getVolumePaths(boolean forceRead) {
        if (mPathList == null || mPathList.isEmpty() || forceRead) {
            mPathList = parseVolumePathsInner();
        }
        return mPathList;
    }

    private List<MyStorageVolume> parseVolumePathsInner() {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            return getVolumePaths13();
        }else{
            return getVolumePaths14();
        }
    }

    private List<MyStorageVolume> getVolumePaths13() {
        return null;
    }

    private List<MyStorageVolume> getVolumePaths14() {
        List<MyStorageVolume> pathsList = new ArrayList<>();
        StorageManager storageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method method = StorageManager.class.getDeclaredMethod("getVolumeList");
            method.setAccessible(true);
            Object[] result = (Object[]) method.invoke(storageManager);
            if (result != null) {
                for(Object object : result){
                    MyStorageVolume myStorageVolume = makeStorageVolume(object);
                    if(myStorageVolume != null){
                        pathsList.add(myStorageVolume);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pathsList;
    }

    private MyStorageVolume makeStorageVolume(Object object){
        try {
            MyStorageVolume storageVolume = new MyStorageVolume();
            Method pathMethod = object.getClass().getDeclaredMethod("getPath");
            storageVolume.path = (String)pathMethod.invoke(object);
            Method removableMethod = object.getClass().getDeclaredMethod("isRemovable");
            storageVolume.removable = (Boolean)removableMethod.invoke(object);

            return storageVolume;
        } catch (Exception e) {
        }
        return null;
    }

    public static class MyStorageVolume {
        public String path;
        public boolean removable;

        @Override
        public String toString() {
            return "MyStorageVolume{" +
                    "path='" + path + '\'' +
                    ", removable=" + removable +
                    '}';
        }
    }


}
