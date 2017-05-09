package com.gmugu.intelliapp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.gmugu.intelliapp.msgrecvive.AlarmReceiver;

/**
 * Created by mugu on 17/4/30.
 */

public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(TAG, "MyApplication onCreate");
        super.onCreate();
        stareMsgService();

    }

    private void stareMsgService() {
        Intent intent1 = new Intent(this, AlarmReceiver.class);
        intent1.setAction("com.gmugu.alarm.action");
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,
                intent1, 0);
        long firstime = SystemClock.elapsedRealtime();
        AlarmManager am = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        // 10秒一个周期，不停的发送广播
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime,
                10 * 1000, sender);
    }

}
