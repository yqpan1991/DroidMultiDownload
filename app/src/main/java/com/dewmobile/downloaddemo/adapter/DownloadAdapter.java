package com.dewmobile.downloaddemo.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dewmobile.downloaddemo.R;
import com.dewmobile.downloaddemo.biz.DownloadManager;
import com.dewmobile.downloaddemo.biz.db.DownloadDatabaseHelper;
import com.dewmobile.downloaddemo.biz.db.DownloadInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by panyongqiang on 16/4/12.
 */
public class DownloadAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> mDataList = new ArrayList<>();
    private DownloadManager mDownloadManager;

    private HashMap<String,DownloadInfo> mDownloadMap;

    public DownloadAdapter(Context context){
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mDownloadManager = DownloadManager.getInstance();
        mDownloadMap = new HashMap<>();
    }

    public void setList(List<String> list){
        mDataList.clear();
        if(list != null && !list.isEmpty()){
            mDataList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void updateDownloadInfo(DownloadInfo downloadInfo){
        mDownloadMap.put(downloadInfo.downloadUrl, downloadInfo);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public String getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.download_item, parent, false);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.pbProgress = (ProgressBar) convertView.findViewById(R.id.pb_progress);
            viewHolder.tvProgress = (TextView) convertView.findViewById(R.id.tv_progress);
            viewHolder.tvStatus = (TextView) convertView.findViewById(R.id.tv_status);
            viewHolder.etUrl = (EditText) convertView.findViewById(R.id.et_url);
            viewHolder.btDownload = (Button) convertView.findViewById(R.id.bt_download);
            viewHolder.btStop = (Button) convertView.findViewById(R.id.bt_stop);
            viewHolder.btOpen = (Button) convertView.findViewById(R.id.bt_open);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        String url = getItem(position);
        viewHolder.etUrl.setText(url);
        viewHolder.btOpen.setEnabled(false);
        if(mDownloadMap.containsKey(url)){
            final DownloadInfo downloadInfo = mDownloadMap.get(url);
            Log.e("DownloadAdapter", downloadInfo.status+"");
            if(downloadInfo.totalSize != 0){
                viewHolder.tvProgress.setText(downloadInfo.currentSize*100/downloadInfo.totalSize+"%\n"+ Formatter.formatFileSize(mContext,downloadInfo.currentSize)+"/"+ Formatter.formatFileSize(mContext,downloadInfo.totalSize));
                viewHolder.pbProgress.setProgress((int) (downloadInfo.currentSize*100/downloadInfo.totalSize));
                if(downloadInfo.status == DownloadDatabaseHelper.STATUS_FINISHED){
                    viewHolder.btOpen.setEnabled(true);
                    viewHolder.btOpen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openFile(downloadInfo);
                        }
                    });
                }
            }else{
                viewHolder.tvProgress.setText("0");
                viewHolder.pbProgress.setProgress(0);

            }
            //update info is ok


        }else{
            //normal info
            viewHolder.tvProgress.setText("null");
            viewHolder.pbProgress.setProgress(0);
        }
        viewHolder.btDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getItem(position);
                mDownloadManager.downloadByNormal(url);
            }
        });
        viewHolder.btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getItem(position);
                mDownloadManager.pause(mDownloadMap.get(url));
            }
        });
        return convertView;
    }

    private void openFile(DownloadInfo downloadInfo) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(fileExt((downloadInfo.downloadUrl)));
        newIntent.setDataAndType(Uri.fromFile(new File(downloadInfo.localPath)),mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
    }

    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    public static class ViewHolder{
        ProgressBar pbProgress;
        TextView tvProgress;
        TextView tvStatus;
        EditText etUrl;
        Button btDownload;
        Button btStop;
        Button btOpen;
    }
}
