package osp.moon.funsflashlight.customobjects;

public abstract class FanColor {
    private int collectionId;
    private int id;
    private String title;
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
