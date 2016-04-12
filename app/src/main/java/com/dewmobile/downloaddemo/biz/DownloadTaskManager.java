package com.dewmobile.downloaddemo.biz;

import android.util.Log;

import com.dewmobile.downloaddemo.biz.db.DownloadCallbackWrapper;
import com.dewmobile.downloaddemo.biz.db.DownloadInfo;
import com.dewmobile.downloaddemo.biz.db.DownloadTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 需要对当前正在执行进行管理,暂停的任务直接便消失
 * 获取当前执行的队列
 * Created by Panda on 2016/4/12.
 */
public class DownloadTaskManager {

    private List<DownloadTask> mRunningList;

    private DownloadTask.DownloadCallback mDownloadCallback;
    private DownloadTask.DownloadCallback mDownloadCallbackWrapper;

    public DownloadTaskManager(DownloadTask.DownloadCallback downloadCallback){
        mRunningList = new ArrayList<>();
        mDownloadCallback = downloadCallback;
        mDownloadCallbackWrapper = newDownloadCallbackWrapper(mDownloadCallback);
    }


    public void addDownload(DownloadInfo downloadInfo){
        DownloadTask task = new DownloadTask(downloadInfo, mDownloadCallbackWrapper);
        if(mRunningList.contains(task)){
            return;
        }
        mRunningList.add(task);
        ThreadPoolManager.getInstance().getDownloadThreadPool().execute(task);
    }

    public void pauseDownload(DownloadInfo downloadInfo){
        for(DownloadTask task : mRunningList){
            if(task.getDownloadInfo().equals(downloadInfo)){
                task.pause();
                return;
            }
        }
    }

    public void deleteDownload(DownloadInfo downloadInfo){
        for(DownloadTask task : mRunningList){
            if(task.getDownloadInfo().equals(downloadInfo)){
                task.delete();
                return;
            }
        }
    }


    private DownloadTask.DownloadCallback newDownloadCallbackWrapper(DownloadTask.DownloadCallback mDownloadCallback) {
        return new DownloadCallbackWrapper(mDownloadCallback) {

            @Override
            protected void onDownloadStartInner(DownloadTask downloadTask, DownloadInfo downloadInfo) {

            }

            @Override
            public void onDownloadPausedInner(DownloadTask downloadTask, DownloadInfo downloadInfo) {
                mRunningList.remove(downloadTask);
            }

            @Override
            public void onDownloadFailedInner(DownloadTask downloadTask, DownloadInfo downloadInfo) {
                mRunningList.remove(downloadTask);
            }

            @Override
            public void onDownloadSucceedInner(DownloadTask downloadTask, DownloadInfo downloadInfo) {
                mRunningList.remove(downloadTask);
            }

            @Override
            protected void onDownloadCanceledInner(DownloadTask downloadTask, DownloadInfo downloadInfo) {
                mRunningList.remove(downloadTask);
            }
        };
    }

}
