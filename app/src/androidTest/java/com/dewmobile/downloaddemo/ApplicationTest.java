package com.dewmobile.downloaddemo;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.edus.utils.NetworkUtils;
import com.edus.utils.StorageHelper;
import com.edus.utils.StorageVolumeHelper;

import java.io.File;
import java.io.IOException;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    private final String TAG = this.getClass().getSimpleName();

    public ApplicationTest() {
        super(Application.class);
    }

    public void testVolumes(){
        StorageHelper helper = StorageHelper.getInstance();

        Log.e(TAG,"download path:"+helper.getDownloadPath());
        Log.e(TAG,"download exist:"+(new File(helper.getDownloadPath()).exists()));

    }

    public void testPath(){
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        Log.e(TAG,"external is directory:"+externalStorageDirectory.isDirectory()+",path:"+externalStorageDirectory.getAbsolutePath());
        Log.e(TAG,"sdcard state:"+Environment.getExternalStorageState());
        File file = new File(externalStorageDirectory.getAbsolutePath()+"/"+"a.txt");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e(TAG,"path:"+file.getAbsolutePath()+",exist:"+file.exists());
        }
        file = new File(externalStorageDirectory.getAbsolutePath()+"/edus/");
        Log.e(TAG,"path:"+file.getAbsolutePath()+",exist:"+file.exists());
        if(!file.exists()){
            file.mkdirs();
        }
        Log.e(TAG,"path:"+file.getAbsolutePath()+",exist:"+file.exists());

    }

    public void testNetwork(){
        Context context = getContext();
        Log.e(TAG, "wifi:"+ NetworkUtils.isWifiAvailable(context));
        Log.e(TAG, "2g:"+NetworkUtils.is2GAvailable(context));
        Log.e(TAG, "3g:"+NetworkUtils.is3GAvailable(context));
        Log.e(TAG, "4g:"+NetworkUtils.is4GAvailable(context));
        Log.e(TAG, "wifiOr4G:"+NetworkUtils.isWifiOr4GAvailable(context));
    }

/*    public void testVolumesByEnvironment(){
        Device[] devices = Environment3.getDevices(null, true, true, true);
        Log.e(TAG,"devices:"+ Arrays.asList(devices).toString());
    }*/
}