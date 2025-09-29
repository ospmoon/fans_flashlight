package osp.moon.funsflashlight.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import osp.moon.funsflashlight.R;
import osp.moon.funsflashlight.customobjects.AnimatedColor;
import osp.moon.funsflashlight.customobjects.FanCollection;
import osp.moon.funsflashlight.customobjects.FanColor;
import osp.moon.funsflashlight.customobjects.FanImage;
import osp.moon.funsflashlight.customobjects.SolidColor;

public class AppHelper {

    private static final String TAG = AppHelper.class.getName();

    public static List<FanCollection> getAssetsColors(final Context context) throws Exception {
        List<FanCollection> result = new ArrayList<>();

        String jsonString = readAsset(context, "colors.json");

        JSONArray jsonArray = new JSONArray(jsonString);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject collectionItem = jsonArray.getJSONObject(i);

            int collectionId = collectionItem.getInt("id");
            String collectionTitle = collectionItem.getString("title");
            JSONArray colors = collectionItem.getJSONArray("colors");
            List<FanColor> colorsList = new ArrayList<>();
            for (int j = 0; j < colors.length(); j++) {
                JSONObject colorItem = colors.getJSONObject(j);
                int id = colorItem.getInt("id");
                String title = colorItem.getString("title");
                FanColor fanColor = null;
                if (colorItem.has("value")) {
                    String value = colorItem.getString("value");
                    fanColor = new SolidColor(collectionId, id, title, value);
                } else if (colorItem.has("ids")) {
                    JSONArray ids = colorItem.getJSONArray("ids");
                    List<Integer> idsList = new ArrayList<>();
                    for (int k = 0; k < ids.length(); k++) {
                        idsList.add(ids.getInt(k));
                    }
                    int delay = 1000;
                    if (colorItem.has("delay")) {
                        delay = colorItem.getInt("delay");
                    }
                    fanColor = new AnimatedColor(collectionId, id, title, idsList, delay);
                } else if (colorItem.has("path")) {
                    String path = colorItem.getString("path");
                    String fileName = colorItem.getString("filename");
                    fanColor = new FanImage(collectionId, id, title, path, fileName);
                }
                if (fanColor == null) continue;
                colorsList.add(fanColor);
            }
            FanCollection fanCollection = new FanCollection(collectionId, collectionTitle, colorsList);
            result.add(fanCollection);
        }
        return result;
    }

    public static String readAsset(final Context context, final String fileName) {
        AssetManager assets = context.getAssets();
        if (assets == null) return null;
        try (InputStream is = assets.open(fileName)) {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            return new String(buffer);
        } catch (IOException e) {
            Log.e(TAG, "readAsset: ", e);
        }
        return null;
    }
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
