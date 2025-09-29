package osp.moon.funsflashlight.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

import osp.moon.funsflashlight.R;
import osp.moon.funsflashlight.helpers.AppHelper;

public class MyBackgroundService extends Service {

    private final String TAG = MyBackgroundService.class.getName();
    public static final String ACTION_START = "osp.moon.clonescreen.ACTION_START";
    public static final String ACTION_STOP = "osp.moon.clonescreen.ACTION_STOP";
    private static final int SERVICE_ID = 1;
    private static AtomicBoolean isRunning = new AtomicBoolean(false);

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        try {
            init();
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Exception.", e);
        }
    }

    private void init() {
        Log.d(TAG, "init()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = AppHelper.getNotification(getApplicationContext(), getApplicationContext().getString(R.string.app_name));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(SERVICE_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(SERVICE_ID, notification);
            }
        } else {
            startForeground(SERVICE_ID, new Notification());
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_NOT_STICKY;
        }
        String action = intent.getAction();
        Log.d(TAG, "onStartCommand: " + action);

        switch (action) {
            case ACTION_START:
                if (isRunning.get()) {
                    Log.w(TAG, "The service is already launched. Exit");
                    return START_NOT_STICKY;
                }

                break;
            case ACTION_STOP:
                stopAllAndSelf("ACTION_STOP");
                break;
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy()");
    }


    private void stopAllAndSelf(String reason) {
        Log.d(TAG, "stopAllAndSelf(), reason: " + reason);

        stopSelf();
    }

}
