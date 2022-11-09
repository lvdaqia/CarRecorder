package com.example.dell.carrecorder;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.dell.carrecorder.Location.Location808;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import androidx.annotation.NonNull;

public class Demo {

/*    public final static String SOCKET_ADDRESS = "scan_socket";
    protected void sendMessage(String msg) {
        LocalSocket sender = new LocalSocket();
        try {
            sender.connect(new LocalSocketAddress("scan_socket"));
            //发送写入数据
            sender.getOutputStream().write(msg.getBytes());
        }catch (Exception e){

        }finally {
            try {
                //关闭socket
                sender.getOutputStream().close();
            }catch (Exception e){

            }

        }

    }*/
    PackageManager mPm;
    HandlerThread ht;
    Handler mHandler;
    String path;
    Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(path != null)
                installAPK(path);
            Log.e("pgyer", "Demo");
            ht.quit();
            return false;
        }
    };
    public Demo(){
        ht = new HandlerThread("install");
        ht.start();
        mHandler = new Handler(ht.getLooper(),callback);
    }

    private static Demo manager = new Demo();
    public static Demo getInstance() {
        return manager;
    }

    public void run(PackageManager mPm,String apkPath){
        this.mPm = mPm;
        path = apkPath;
        mHandler.sendEmptyMessage(1);
    }

    public boolean isAsciiControl(final char ch) {
        return ch < 32 || ch == 127;
    }

    public String toString(char c) {
        return c < ASCII_LENGTH ? CACHE[c] : String.valueOf(c);
    }

    private static final int ASCII_LENGTH = 128;
    private static final String[] CACHE = new String[ASCII_LENGTH];

    public void getChars(String sData){

        for(char c:sData.toCharArray()){
            if(isAsciiControl(c)){
                sData.replace(toString(c),"");
            }
        }
    }

    public boolean installAPK(String apkPath){
        try {
            Class<?> pmClz = mPm.getClass();
            Class<?> aClass = Class.forName("android.app.PackageInstallObserver");
            Constructor<?> constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object installObserver = constructor.newInstance();
            Method method = pmClz.getDeclaredMethod("installPackage", Uri.class, aClass, int.class, String.class);
            method.setAccessible(true);
            method.invoke(mPm, Uri.fromFile(new File(apkPath)), installObserver, 2, null);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private void write_file(char[] data,boolean append){
        FileWriter fw = null;
        final File taisau = new File("/cache/recovery/last_taisau");
        try{
            if (!taisau.exists()){
                taisau.createNewFile();
            }
            fw = new FileWriter(taisau,append);
            fw.write(data);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(fw != null) {
                    fw.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private String read_file(){
        final File taisau = new File("/cache/recovery/last_taisau");
        FileReader fr = null;
        String result = "";
        char[] buffer = new char[1024];
        int len = -1;
        try{
            fr = new FileReader(taisau);
            while((len = fr.read(buffer)) != -1){
                result += new String(buffer,0,len);//去char数组cbuf，从0开始取，取len个
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(fr != null) {
                    fr.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }


    public class PushReceiver extends BroadcastReceiver {
        private static final String TAG = "PushReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if("com.eastaeon.action.WRITE_DATA".equals(intent.getAction())){
                    char[] data = intent.getCharArrayExtra("data");
                    boolean append = intent.getBooleanExtra("APPEND",false);
                    write_file(data,append);
                }else if("com.eastaeon.action.READ_DATA".equals(intent.getAction())){
                    String result = read_file();
                    Intent customer = new Intent("com.eastaeon.action.FILETEXT");
                    customer.putExtra("filetext",result.toCharArray());
                    context.sendBroadcast(customer);
                }else {

                }

            } catch (Exception e) {

            }

        }
    };
}
