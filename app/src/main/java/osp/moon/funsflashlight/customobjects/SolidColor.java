package osp.moon.funsflashlight.customobjects;

import android.graphics.Color;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class SolidColor extends FanColor {
    private final String value;
    public int getColor() {
        try {
            return Color.parseColor(value);
        } catch (Exception e) {
            return Color.WHITE;
        }
    }
    public SolidColor(int collectionId, int id, String title, String value) {
        super(collectionId, id, title);
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", getId());
            jsonObject.put("title", getTitle());
            jsonObject.put("value", value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
