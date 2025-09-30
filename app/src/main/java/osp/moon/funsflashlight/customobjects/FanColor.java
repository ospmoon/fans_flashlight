package osp.moon.funsflashlight.customobjects;

import java.io.Serializable;

public abstract class FanColor implements Serializable {
    private final int id;
    private final int collectionId;
    private final String title;
    public FanColor(int collectionId, int id, String title) {
        this.collectionId = collectionId;
        this.id = id;
        this.title = title;
    }
    public int getCollectionId() {
        return collectionId;
    }
    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
}
