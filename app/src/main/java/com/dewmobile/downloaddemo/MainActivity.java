package com.dewmobile.downloaddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dewmobile.downloaddemo.biz.DownloadBroadHelper;
import com.dewmobile.downloaddemo.biz.DownloadManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    @Bind(R.id.pb_progress)
    ProgressBar mPbProgress;
    @Bind(R.id.et_url)
    EditText mEtUrl;
    @Bind(R.id.bt_operation)
    Button mBtOperation;
    @Bind(R.id.tv_status)
    TextView tvStatus;
    @Bind(R.id.tv_progress)
    TextView tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadBroadHelper.ACTION_DOWNLOAD_PROGRESS);
        filter.addAction(DownloadBroadHelper.ACTION_DOWNLOAD_STATUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadReceiver, filter);
    }

    private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DownloadBroadHelper.ACTION_DOWNLOAD_PROGRESS)) {
                long current = intent.getLongExtra(DownloadBroadHelper.EXTRA_CURRENT, 0);
                long total = intent.getLongExtra(DownloadBroadHelper.EXTRA_TOTAL, 0);
                onProgressChangedImpl(current, total);
            } else if (action.equals(DownloadBroadHelper.ACTION_DOWNLOAD_STATUS)) {
                DownloadManager.DownloadStatus status = (DownloadManager.DownloadStatus) intent.getSerializableExtra(DownloadBroadHelper.EXTRA_STATUS);
                onStatusChangedImpl(status);
            }
        }
    };

    @OnClick(R.id.bt_operation)
    public void onClick() {
        String url = mEtUrl.getText().toString();
        if (url != null) {
//            DownloadManager.getInstance().downloadByNormal(url);
            DownloadManager.getInstance().download(url);
        }
    }

    private void onProgressChangedImpl(long current, long total) {
        mPbProgress.setProgress((int) (current * 100 / total));
        Log.e(TAG, "current:" + current + ",total:" + total);
        tvProgress.setText(current*100/total+"%\n"+ Formatter.formatFileSize(this,current)+"/"+Formatter.formatFileSize(this,total));
    }

    private void onStatusChangedImpl(DownloadManager.DownloadStatus currentStatus) {
        if (currentStatus == DownloadManager.DownloadStatus.INIT) {
            tvStatus.setText("初始化");
        } else if (currentStatus == DownloadManager.DownloadStatus.DOWNLOADING) {
            tvStatus.setText("下载中");
        } else if (currentStatus == DownloadManager.DownloadStatus.ERROR) {
            tvStatus.setText("错误");
        } else if (currentStatus == DownloadManager.DownloadStatus.PAUSE) {
            tvStatus.setText("暂停");
        } else if (currentStatus == DownloadManager.DownloadStatus.SUCCESS) {
            tvStatus.setText("下载成功");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadReceiver);
    }
}
