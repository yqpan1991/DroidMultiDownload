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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dewmobile.downloaddemo.adapter.DownloadAdapter;
import com.dewmobile.downloaddemo.biz.DownloadBroadHelper;
import com.dewmobile.downloaddemo.biz.DownloadManager;
import com.dewmobile.downloaddemo.biz.db.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();


    private List<String> mDownloadInfoList;
    private DownloadAdapter mDownloadAdapter;

    private ListView mListView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.lv_content);
        initUrlList();
        mDownloadAdapter = new DownloadAdapter(this);
        mDownloadAdapter.setList(mDownloadInfoList);
        mListView.setAdapter(mDownloadAdapter);
        //register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadBroadHelper.ACTION_DOWNLOAD_PROGRESS);
        filter.addAction(DownloadBroadHelper.ACTION_DOWNLOAD_STATUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadReceiver, filter);
    }

    private void initUrlList() {
        mDownloadInfoList = new ArrayList<>();
        mDownloadInfoList.add("https://d3fwkemdw8spx3.cloudfront.net/sqlite/SQLProSQLite.1.0.69.app.zip");
        mDownloadInfoList.add("http://downloadb.dewmobile.net/centersrc/20160402/d5c22583c075c44ab72110a2eed846b2-085534.flv");
        mDownloadInfoList.add("http://xz.job391.com/down/Everything@89_1_1468.exe");
        mDownloadInfoList.add("http://download.calibre-ebook.com/videos/grand-tour.mp4");
        mDownloadInfoList.add("http://hot.m.shouji.360tpcdn.com/160318/dccf2943b210d6ba26e994a324e8b38a/com.qihoo.appstore_300050091.apk");
        mDownloadInfoList.add("http://7.hunlang.com/kkk12/%E6%B8%B8%E6%88%8F%E7%8E%AF%E5%A2%83%E7%BB%84%E4%BB%B6%E5%AE%89%E8%A3%85%E5%8C%85.exe");
    }

    private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
/*            String action = intent.getAction();
                if (action.equals(DownloadBroadHelper.ACTION_DOWNLOAD_PROGRESS)) {
                DownloadInfo mDownloadInfo = (DownloadInfo) intent.getSerializableExtra(DownloadBroadHelper.EXTRA_FILE);
                mDownloadAdapter.updateDownloadInfo(mDownloadInfo);
            } else if (action.equals(DownloadBroadHelper.ACTION_DOWNLOAD_STATUS)) {
                DownloadInfo mDownloadInfo = (DownloadInfo) intent.getSerializableExtra(DownloadBroadHelper.EXTRA_FILE);
                mDownloadAdapter.updateDownloadInfo(mDownloadInfo);
            }*/
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDownloadAdapter.destroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadReceiver);
    }
}
