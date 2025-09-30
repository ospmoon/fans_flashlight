package osp.moon.funsflashlight.customobjects;

import android.graphics.Color;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.Serializable;

public class SolidColor extends FanColor implements Serializable {
    private final String value;

    public SolidColor(int collectionId, int id, String title, String value) {
        super(collectionId, id, title);
        this.value = value;
    }

    public int getColor() {
        try {
            return Color.parseColor(this.value);
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    public String getValue() {
        return this.value;
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", getId());
            jsonObject.put("title", getTitle());
            jsonObject.put("value", getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
