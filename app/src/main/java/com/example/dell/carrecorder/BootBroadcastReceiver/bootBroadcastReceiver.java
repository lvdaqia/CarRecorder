package com.example.dell.carrecorder.BootBroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.dell.carrecorder.RecorActivity;

public class bootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootBroadcast","~~~~");
        if (intent.getAction().equals(ACTION)) {
//            Intent mainActivityIntent = new Intent(context, RecorActivity.class);  // 要启动的Activity
//            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(mainActivityIntent);
        }
    }

}
