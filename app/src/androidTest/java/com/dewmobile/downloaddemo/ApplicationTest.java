package com.dewmobile.downloaddemo;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.edus.utils.NetworkUtils;
import com.edus.utils.StorageHelper;
import com.edus.utils.StorageVolumeHelper;

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