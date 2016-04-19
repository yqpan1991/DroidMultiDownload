package com.dewmobile.downloaddemo.biz.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dewmobile.downloaddemo.MyApplication;

/**
 * Created by panyongqiang on 16/4/11.
 */
public class DownloadDatabaseHelper extends SQLiteOpenHelper {

    public static String COLUMN_ID = "_id";
    public static String COLUMN_DOWNLOAD_URL = "downloadUrl";
    public static String COLUMN_LOCAL_PATH = "localPath";
    public static String COLUMN_NET_TYPE = "netType";
    public static String COLUMN_STATUS = "status";
    public static String COLUMN_TOTAL_SIZE = "totalSize";
    public static String COLUMN_CURRENT_SIZE = "currentSize";

    public static int STATUS_PENDING = 0;
    public static int STATUS_DOWNLOADING = 1;
    public static int STATUS_PAUSE = 2;
    public static int STATUS_FINISHED = 3;
    public static int STATUS_NET_ERROR = 4;
    public static int STATUS_NORMAL_ERROR = 5;
    public static int STATUS_CANCELED = 6;


    private static int dbVersion = 1;
    private static String downloadDbName = "download.db";
    private static String downloadTableName = "download";

    private static DownloadDatabaseHelper instance;

    public static DownloadDatabaseHelper getInstance() {
        if (instance == null) {
            synchronized (DownloadDatabaseHelper.class) {
                if (instance == null) {
                    instance = new DownloadDatabaseHelper(MyApplication.getContext());
                }
            }
        }
        return instance;
    }

    public DownloadDatabaseHelper(Context context) {
        super(context, downloadDbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        buildDownloadTable(db);
    }

    private void buildDownloadTable(SQLiteDatabase db) {

        db.execSQL("drop table  if exists "+downloadTableName);

        db.execSQL("create table " + downloadTableName + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY autoincrement," +
                COLUMN_DOWNLOAD_URL + " TEXT," +
                COLUMN_LOCAL_PATH + " TEXT," +
                COLUMN_NET_TYPE + " INTEGER," +
                COLUMN_STATUS + " INTEGER," +
                COLUMN_TOTAL_SIZE + " INTEGER," +
                COLUMN_CURRENT_SIZE + " INTEGER" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        buildDownloadTable(db);
    }

    public DownloadBean queryDownloadBean(String url){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(downloadTableName, null, COLUMN_DOWNLOAD_URL + "=?", new String[]{url}, null, null, null);
        if(cursor != null && cursor.getCount() > 0 ){
            cursor.moveToFirst();
            return new DownloadBean(cursor, new DownloadColumnIndex(cursor));
        }
        cursor.close();
        return null;
    }

    public boolean addDownloadRecord(DownloadBean bean){
        ContentValues values = new ContentValues();
        values.put(COLUMN_DOWNLOAD_URL, bean.downloadUrl);
        values.put(COLUMN_LOCAL_PATH, bean.localPath);
        values.put(COLUMN_NET_TYPE, bean.netType);
        values.put(COLUMN_STATUS, 0);
        values.put(COLUMN_TOTAL_SIZE, bean.totalSize);
        values.put(COLUMN_CURRENT_SIZE, 0);
        SQLiteDatabase db = getWritableDatabase();
        long rowId =  db.insert(downloadTableName, null, values);
        bean.id = rowId;
        return rowId >= 0;
    }

    public boolean deleteDownload(long transferId){
        return getWritableDatabase().delete(downloadTableName, COLUMN_ID+"=?",new String[]{transferId+""}) > 0;
    }

    public void updateValues(long downloadId, ContentValues values){
        SQLiteDatabase db = getWritableDatabase();
        db.update(downloadTableName, values, COLUMN_ID+"=?", new String[]{downloadId+""});

    }

    public void updateProgress(long downloadId, long currentSize){
        ContentValues values = new ContentValues();
        values.put(COLUMN_CURRENT_SIZE, currentSize);
        updateValues(downloadId,values);
   }

    public void updateTotal(long downloadId, long totalSize){
        ContentValues values = new ContentValues();
        values.put(COLUMN_TOTAL_SIZE, totalSize);
        updateValues(downloadId,values);
    }



    //需要实现增删改查的动作
    //根据url获取信息
    //数据，id，下载的url，本地路径，网络类型，当前的下载状态,总长度，当前的长度
    //_id,url,localpath,networktype,downloadStatus,totalSize,currentSize

    public static class DownloadColumnIndex {
        public int indexId;
        public int indexUrl;
        public int indexLocalPath;
        public int indexNetType;
        public int indexStatus;
        public int indexTotalSize;
        public int indexCurrentSize;

        public DownloadColumnIndex(Cursor cursor){
            indexId = cursor.getColumnIndex(COLUMN_ID);
            indexUrl = cursor.getColumnIndex(COLUMN_DOWNLOAD_URL);
            indexLocalPath = cursor.getColumnIndex(COLUMN_LOCAL_PATH);
            indexNetType = cursor.getColumnIndex(COLUMN_NET_TYPE);
            indexStatus = cursor.getColumnIndex(COLUMN_STATUS);
            indexTotalSize = cursor.getColumnIndex(COLUMN_TOTAL_SIZE);
            indexCurrentSize = cursor.getColumnIndex(COLUMN_CURRENT_SIZE);
        }

    }
}
