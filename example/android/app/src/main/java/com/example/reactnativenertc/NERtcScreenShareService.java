package com.example.reactnativenertc;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

@TargetApi(21)
public class NERtcScreenShareService extends Service {
    private static final String TAG = "NERtcScreenShareService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "NERtcScreenShareService";

    private ScreenShareBinder mScreenShareBinder;

    public NERtcScreenShareService() {
        mScreenShareBinder = new ScreenShareBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand ");
        startForeground();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind ");
        startForeground();
        return mScreenShareBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        stopForeground(true);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        stopForeground(true);
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription(CHANNEL_ID);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification getForeNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), getApplicationContext().getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = new Notification.Builder(getApplicationContext())
            .setContentTitle(CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setContentText(CHANNEL_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();
    }

    public class ScreenShareBinder extends Binder {
        public NERtcScreenShareService getService() {
            return NERtcScreenShareService.this;
        }
    }

    private void startForeground() {

        createNotificationChannel();
        Notification notification = getForeNotification();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.i(TAG, "sdkVer:" + Build.VERSION.SDK_INT + " using FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION");
            try {
                startForeground(NOTIFICATION_ID, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                stopForeground(true);
                startForeground(NOTIFICATION_ID, notification);
            }

        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }
}
