package com.dewmobile.downloaddemo.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dewmobile.downloaddemo.R;
import com.dewmobile.downloaddemo.biz.DownloadManager;
import com.dewmobile.downloaddemo.biz.db.DownloadInfo;

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
        }
        viewHolder = (ViewHolder) convertView.getTag();
        String url = getItem(position);
        viewHolder.etUrl.setText(url);
        if(mDownloadMap.containsKey(url)){
            DownloadInfo downloadInfo = mDownloadMap.get(url);
            if(downloadInfo.totalSize != 0){
                viewHolder.tvProgress.setText(downloadInfo.currentSize*100/downloadInfo.totalSize+"%\n"+ Formatter.formatFileSize(mContext,downloadInfo.currentSize)+"/"+ Formatter.formatFileSize(mContext,downloadInfo.totalSize));
                viewHolder.pbProgress.setProgress((int) (downloadInfo.currentSize*100/downloadInfo.totalSize));
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

    public static class ViewHolder{
        ProgressBar pbProgress;
        TextView tvProgress;
        TextView tvStatus;
        EditText etUrl;
        Button btDownload;
        Button btStop;
    }
}
