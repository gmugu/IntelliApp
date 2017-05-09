package com.gmugu.intelliapp.msgrecvive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by mugu on 17/5/4.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG,"alarm onReceive");
        if (intent.getAction().equals("com.gmugu.alarm.action")) {
            Intent i = new Intent();
            i.setClass(context, MessageService.class);
            // 启动service
            // 多次调用startService并不会启动多个service 而是会多次调用onStart
            context.startService(i);
        }
    }
}
