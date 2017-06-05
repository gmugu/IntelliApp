package com.gmugu.intelliapp.msgrecvive;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.SoundPool;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gmugu.intelliapp.R;

/**
 * Created by mugu on 17/5/4.
 */

public class MessageService extends Service {
    private final static String TAG = MessageService.class.getSimpleName();
    private Recviver pushRecviver;
    private SharedPreferences defaultSharedPreferences;
    private SoundPool soundPool;
    private int tsSoundId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "***** MessageService *****: onCreate");
        super.onCreate();
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        soundPool = new SoundPool(2,1,0);
        tsSoundId = soundPool.load(this, R.raw.ts,1);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.v(TAG, "***** MessageService *****: onStart");
        // 这里可以做Service该做的事
        if (pushRecviver == null || !pushRecviver.isRunning()) {
            pushRecviver = new Recviver(this, 9406, new Recviver.OnRecviveData() {
                @Override
                public void onRecviveData(byte[] data) {
                    Log.d(TAG, new String(data));
                    boolean isNotification = defaultSharedPreferences.getBoolean(getResources().getString(R.string.key_is_notification), true);
                    if (isNotification) {
                        sendNotification("通知", "访客来访");
                    }
                }
            });
            pushRecviver.startRecvive();
        }
    }

    private static int notifyId = 0;

    private void sendNotification(String title, String text) {
        NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification noti = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                .getNotification();
        notifyManager.notify(++notifyId, noti);
        soundPool.play(tsSoundId,1,1,0,0,1);
    }
}
