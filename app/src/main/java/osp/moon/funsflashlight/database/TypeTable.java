package osp.moon.funsflashlight.database;

public enum TypeTable {
    COLLECTION_TABLE("collection_table"),
    COLOR_TABLE("color_table"),
    COLOR_IDS_TABLE("color_ids_table");

    private final String stringValue;

    TypeTable(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

}
