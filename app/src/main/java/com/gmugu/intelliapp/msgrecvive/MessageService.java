package com.gmugu.intelliapp.msgrecvive;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gmugu.intelliapp.R;

/**
 * Created by mugu on 17/5/4.
 */

public class MessageService extends Service {
    private final static String TAG = MessageService.class.getSimpleName();
    private Recviver pushRecviver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "***** MessageService *****: onCreate");
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
                    sendNotification("通知", new String(data));
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
    }
}
