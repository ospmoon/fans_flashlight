package osp.moon.funsflashlight.customobjects;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.Serializable;

public class FanImage extends FanColor implements Serializable {
    public static final String ASSETS = "assets";
    private final String path;
    private final String fileName;

    public FanImage(int collectionId, int id, String title, String path, String fileName) {
        super(collectionId, id, title);
        this.path = path;
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", getId());
            jsonObject.put("title", getTitle());
            jsonObject.put("path", getPath());
            jsonObject.put("fileName", getFileName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
