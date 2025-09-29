package osp.moon.funsflashlight.customobjects;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FanCollection {
    private final int id;
    private final String title;
    private final List<FanColor> colorList;

    public FanCollection(int id, String title, List<FanColor> colorList) {
        this.id = id;
        this.title = title;
        this.colorList = new ArrayList<>(colorList);
    }
    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public List<FanColor> getColorList() {
        return colorList;
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", getId());
            jsonObject.put("title", getTitle());
            jsonObject.put("colorList", getColorList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
