package com.dewmobile.downloaddemo.biz;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.dewmobile.downloaddemo.MyApplication;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 初始化下载的信息
 * 2. 检测当前的url是否在下载的数据库中，如果在，从数据库获取初始化文件的信息，检查状态位，如果是处于下载的队列中不做处理，否则，更改将其更改为排队下载中
 * 3. 如果没有在下载的记录中，生成完整的文件信息，然后生成一个下载的记录，插入到数据库中，然后从网络获取数据，如果数据获取完整，
 * 同样插入到数据库中，然后将状态位更改为排队下载中
 * Created by panyongqiang on 16/4/8.
 */
public class InitDownloadInfoTask implements Runnable {

    private String url;
    private Context mContext;
    private FileInfo fileInfo;
    private InitCallback mCallback;

    private final String TAG = this.getClass().getSimpleName();

    public InitDownloadInfoTask(Context context, String url, InitCallback callback) {
        this.url = url;
        mContext = context;
        mCallback = callback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(url)) {
            //notify error
            //insert into database,error
            //向外广播即可，失败
            return;
        }
        //
        fileInfo = new FileInfo();
        fileInfo.url = url;
        fileInfo.generateLocalPath(MyApplication.getContext());
        if (fileInfo.fileSize > 0) {
            //insert into database
            //init done
        } else {
            fetchInfoFromInternet();
        }

    }

    private void fetchInfoFromInternet() {
        //fetch from internet
        URL url = null;
        try {
            url = new URL(fileInfo.url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url == null) {
            //notify error
            mCallback.initFailed();
            return;
        }
        int contentLength = 0;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30 * 1000);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                contentLength = connection.getContentLength();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        if (contentLength <= 0) {
            //error
            mCallback.initFailed();
            return;
        } else {
            fileInfo.fileSize = contentLength;
            //insert into database
            //notify parse succeed
            if(mCallback != null){
                mCallback.initSucceed(fileInfo);
            }

        }
        Log.e(TAG, fileInfo.toString());

    }

    private FileInfo checkFromDatabase() {
        return null;
    }

    //需要向外广播出去当前文件的状态
    //在完成后，向外通知当前文件的状态

    public interface  InitCallback{
        void initSucceed(FileInfo fileInfo);
        void initFailed();
    }
}
