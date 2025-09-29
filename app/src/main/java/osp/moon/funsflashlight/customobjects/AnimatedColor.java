package osp.moon.funsflashlight.customobjects;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnimatedColor extends FanColor {
    private final List<Integer> ids;
    private final int delay;

    public AnimatedColor(int collectionId, int id, String title, List<Integer> ids, int delay) {
        super(collectionId, id, title);
        this.ids = new ArrayList<>(ids);
        this.delay = delay;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public int getDelay() {
        return delay;
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", getId());
            jsonObject.put("title", getTitle());
            jsonObject.put("delay", getDelay());
            jsonObject.put("isd", getIds().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
