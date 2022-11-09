package com.example.dell.carrecorder.db;

import android.content.Context;
import android.util.Log;

public class InstanceDBHelper extends DataBaseHelper {

    private static InstanceDBHelper mInstanceDBHelper;

    private InstanceDBHelper(Context context){
        super(context);
    }

    public static InstanceDBHelper getInstance(Context context){
        if (mInstanceDBHelper==null){
            synchronized (DataBaseHelper.class){
                if (mInstanceDBHelper==null){
                    mInstanceDBHelper = new InstanceDBHelper(context);
                    if (mInstanceDBHelper.getDB()==null||!mInstanceDBHelper.getDB().isOpen()){
                        mInstanceDBHelper.open();
                        Log.d("queryAll","open");
                    }
                }
            }
        }
        return mInstanceDBHelper;
    }

    @Override
    protected int getMDbVersion(Context context) {
        return 1;
    }

    @Override
    protected String getDbName(Context context) {
        return "vedio.db";
    }

    @Override
    protected String[] getDbCreateSql(Context context) {
        String[] a = new String[1];
        a[0] = "CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,path TEXT,duration INTEGER,size INTEGER)";
        return a;
    }

    @Override
    protected String[] getDbUpdateSql(Context context) {
        return new String[0];
    }
}