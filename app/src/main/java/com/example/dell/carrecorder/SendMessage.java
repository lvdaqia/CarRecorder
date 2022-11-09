package com.example.dell.carrecorder;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.view.WindowManager;
import android.widget.EditText;
import java.io.OutputStream;
import java.util.List;

public class SendMessage {
    LocalSocket client = null;
    private String name = "input_event";
    OutputStream outputStream;
    public void  init(){
        if(client == null){
            client = new LocalSocket();
        }
        try{
            client.connect(new LocalSocketAddress(name),10000);
            outputStream = client.getOutputStream();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(client != null){
                try {
                    client.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public void add(){
        //outputStream.write();
    }


}
