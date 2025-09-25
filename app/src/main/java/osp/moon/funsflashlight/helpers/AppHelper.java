package osp.moon.funsflashlight.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import osp.moon.funsflashlight.R;

public class AppHelper {

    private static final String TAG = AppHelper.class.getName();


    @RequiresApi(Build.VERSION_CODES.O)
    public static Notification getNotification(Context context, String serviceName) {
        String NOTIFICATION_CHANNEL_ID = "osp.moon.funsflashlight";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, serviceName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(chan);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        return notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(serviceName)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
    }
}
