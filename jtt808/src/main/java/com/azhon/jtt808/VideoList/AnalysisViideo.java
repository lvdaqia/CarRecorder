package com.azhon.jtt808.VideoList;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class AnalysisViideo {
    private Context context;
    private String mResult = new String();
    private String[] mFileList = null;
    private List<vedioBean> vedioBeans;
    public AnalysisViideo(Context context){
        this.context = context;
    }
    public List<vedioBean> findVideoList(String path) {
        File flist = new File(path);
        mFileList = flist.list();
        vedioBeans = new ArrayList<vedioBean>();
        for (String str : mFileList) {
            vedioBean bean = new vedioBean();
            bean.setName(str);
            bean.setPath(path+str);
            try {
                bean.setSize(getFileSize(new File(path+str)));
                vedioBeans.add(bean);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d("findVideo",vedioBeans.size()+"");
        return  vedioBeans;
    }
    public  long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
            Log.d("获取文件大小",size+"");
        } else {
            file.createNewFile();
            Log.d("获取文件大小","文件不存在!");
        }
        return size;
    }
    /**
     * 根据文件名获取视频创建时间
     * @param title
     * @return
     */
    public String getVideoBuildTime(String title){
        Log.d("BuildTime","title:"+title);
        String[] buildTime = title.split("_");
        return buildTime[2].substring(0,14);
    }

    public String getVideoName(String path) {
        String[] file = path.split("/");
        for (int i = 0; i < file.length; i++) {
            if (file[i].contains("YJ_")) {
                Log.d("getVideoName","file[i]:"+file[i]);
                return file[i];
            }
        }
        Log.d("getVideoName","file[i]:???");
        return "";
    }
}
