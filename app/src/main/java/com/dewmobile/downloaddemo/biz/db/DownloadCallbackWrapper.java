package com.dewmobile.downloaddemo.biz.db;

/**
 * Created by panyongqiang on 16/4/12.
 */
public class DownloadCallbackWrapper implements DownloadTask.DownloadCallback {

    private DownloadTask.DownloadCallback mDownloadCallback;

    public DownloadCallbackWrapper(DownloadTask.DownloadCallback downloadCallback){
        mDownloadCallback = downloadCallback;
    }

    @Override
    public final void onDownloadStart(DownloadTask downloadTask, DownloadInfo downloadInfo) {
        onDownloadStartInner(downloadTask, downloadInfo);
        if(mDownloadCallback != null){
            mDownloadCallback.onDownloadStart(downloadTask, downloadInfo);
        }
    }

    protected void onDownloadStartInner(DownloadTask downloadTask, DownloadInfo downloadInfo) {

    }

    @Override
    public final void onDownloadProgressUpdated(DownloadTask downloadTask, DownloadInfo downloadInfo) {
        onDownloadProgressUpdatedInner(downloadTask, downloadInfo);
        if(mDownloadCallback != null){
            mDownloadCallback.onDownloadProgressUpdated(downloadTask, downloadInfo);
        }
    }

    protected void onDownloadProgressUpdatedInner(DownloadTask downloadTask, DownloadInfo downloadInfo) {

    }

    @Override
    public final void onDownloadPaused(DownloadTask downloadTask, DownloadInfo downloadInfo) {
        onDownloadPausedInner(downloadTask, downloadInfo);
        if(mDownloadCallback != null){
            mDownloadCallback.onDownloadPaused(downloadTask, downloadInfo);
        }
    }

    protected void onDownloadPausedInner(DownloadTask downloadTask, DownloadInfo downloadInfo) {

    }

    @Override
    public final void onDownloadFailed(DownloadTask downloadTask, DownloadInfo downloadInfo) {
        onDownloadFailedInner(downloadTask, downloadInfo);
        if(mDownloadCallback != null){
            mDownloadCallback.onDownloadFailed(downloadTask, downloadInfo);
        }
    }

    protected void onDownloadFailedInner(DownloadTask downloadTask, DownloadInfo downloadInfo) {

    }

    @Override
    public final void onDownloadSucceed(DownloadTask downloadTask, DownloadInfo downloadInfo) {
        onDownloadSucceedInner(downloadTask, downloadInfo);
        if(mDownloadCallback != null){
            mDownloadCallback.onDownloadSucceed(downloadTask, downloadInfo);
        }
    }

    @Override
    public final void onDownloadCanceled(DownloadTask downloadTask, DownloadInfo downloadInfo) {
        onDownloadCanceledInner(downloadTask, downloadInfo);
        if(mDownloadCallback != null){
            mDownloadCallback.onDownloadCanceled(downloadTask, downloadInfo);
        }
    }

    protected void onDownloadCanceledInner(DownloadTask downloadTask, DownloadInfo downloadInfo) {

    }

    protected void onDownloadSucceedInner(DownloadTask downloadTask, DownloadInfo downloadInfo) {

    }
}
