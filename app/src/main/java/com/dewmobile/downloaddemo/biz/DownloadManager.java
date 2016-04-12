package com.dewmobile.downloaddemo.biz;

import android.content.Context;
import android.text.TextUtils;

import com.dewmobile.downloaddemo.MyApplication;
import com.dewmobile.downloaddemo.biz.db.DownloadBean;
import com.dewmobile.downloaddemo.biz.db.DownloadInfo;
import com.dewmobile.downloaddemo.biz.db.DownloadTask3;

import java.util.HashMap;

/**
 * Created by panyongqiang on 16/3/31.
 */
public class DownloadManager {

    private HashMap<String,DownloadStatus> downloadStatusMap;
    private static DownloadManager mInstance;
    private Context mContext;
    private ThreadPoolManager threadPoolManager;
    private DownloadBroadHelper downloadBroadHelper;

    private DownloadManager(Context context){
        downloadStatusMap = new HashMap<>();
        mContext = context;
        threadPoolManager = ThreadPoolManager.getInstance();
        downloadBroadHelper = new DownloadBroadHelper(mContext);
    }

    public static DownloadManager getInstance(){
        if(mInstance == null){
            synchronized (DownloadManager.class){
                if(mInstance == null){
                    mInstance = new DownloadManager(MyApplication.getContext());
                }
            }
        }
        return mInstance;
    }


    public DownloadStatus getDownloadStatus(String url){
        if(url == null){
            return null;
        }
        if(downloadStatusMap.containsKey(url)){
            return downloadStatusMap.get(url);
        }
        return DownloadStatus.INIT;
    }


    public void download(String url){
        if(TextUtils.isEmpty(url)){
            return;
        }
        threadPoolManager.getDownloadThreadPool().execute(buildDownload(url));
/*        //根据url，插入到数据库中即可
        //数据库在插入成功后，向外通知插入数据库成功
        //然后开始生成线程，下载文件的概要信息
        //localpath
        //fileSize等等信息
        //下次再次进入时，读取数据库的状态即可

        //文件的概要信息下载完成后
        //生成下载的task，向外通知即可
        //根据下载的url去查询即可
        FileInfo info = new FileInfo();
        info.url = url;
        info.localPath = mContext.getCacheDir()+"/"+ "123.zip";
        DownloadTask task = new DownloadTask();
        task.downloadInfo = info;
        task.callback = callback;
        new Thread(task).start();*/
    }

    public void downloadByNormal(String url){
        if(TextUtils.isEmpty(url)){
            return;
        }
        threadPoolManager.getPreDownloadThreadPool().execute(buildDownloadByNormal(url));
    }



    private Runnable buildDownload(String url){
        Runnable runnable =  new InitDownloadInfoTask(mContext, url, new InitDownloadInfoTask.InitCallback() {
            @Override
            public void initSucceed(FileInfo fileInfo) {
                //build download thread
                //then download
                //同样需要查询数据库的信息
                DownloadTask2 downloadTask = new DownloadTask2();
                downloadTask.downloadInfo = fileInfo;
                downloadTask.run();
            }

            @Override
            public void initFailed() {

            }
        });
        return runnable;
    }

    private Runnable buildDownloadByNormal(String url){
        FileInfo fileInfo = new FileInfo();
        fileInfo.url = url;
        Runnable runnable = new InitDownloadInfoTask2(fileInfo, new InitDownloadInfoTask2.InitCallback(){

            @Override
            public void onNewDownloadRecord(DownloadBean bean) {
                //callback new record
                //then put it to download is ok
                //对于请求的长度
                //放置到线程的请求队列中

                new DownloadTask3(new DownloadInfo(bean)).run();
            }

            @Override
            public void onDownloadRecordExist(DownloadBean bean) {
                //TODO -- check is downloading
                //if is downloading,then do nothing
                //else add new task is ok
                //检查当前的状态,如果是非下载中,通知新重启线程即可
            }

            @Override
            public void onInitError(FileInfo fileInfo) {

            }
        });
        return runnable;
    }

    public void pause(String url){
        //根据url，
        //暂停的话，检测当前的下载是否在运行，如果在运行，那么将其暂停
    }

    public DownloadBroadHelper getDownloadBroadHelper(){
        return downloadBroadHelper;
    }

    public enum DownloadStatus{
        INIT,//
        INIT_NORMAL_ERROR,//can
        INIT_FETAL_ERROR,
        INIT_PAUSE,
        WAITING,//start download
        DOWNLOADING,//downloading
        PAUSE,//pause
        ERROR,//download error
        SUCCESS,//下载成功
    }


}
