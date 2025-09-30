package osp.moon.funsflashlight.database;

import static osp.moon.funsflashlight.AppConstants.ANIMATED_COLOR;
import static osp.moon.funsflashlight.AppConstants.SOLID_COLOR;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import osp.moon.funsflashlight.App;
import osp.moon.funsflashlight.AppConstants;
import osp.moon.funsflashlight.customobjects.AnimatedColor;
import osp.moon.funsflashlight.customobjects.FanCollection;
import osp.moon.funsflashlight.customobjects.FanColor;
import osp.moon.funsflashlight.customobjects.FanImage;
import osp.moon.funsflashlight.customobjects.SolidColor;

public class AppDatabase extends SQLiteOpenHelper {

    private static final String TAG = AppDatabase.class.getName();
    public static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "funsflashlight.db";
    private static AppDatabase _instance;
    public static synchronized AppDatabase getInstance(final Context context) {
        if (_instance == null) {
            _instance = new AppDatabase(context);
        }
        return _instance;
    }

    private final String CREATE_TABLE_COLLECTION = "CREATE TABLE IF NOT EXISTS " + TypeTable.COLLECTION_TABLE + " (" +
            " id integer," +
            " title text" +
            ");";

    private final String CREATE_TABLE_COLOR = "CREATE TABLE IF NOT EXISTS " + TypeTable.COLOR_TABLE + " (" +
            " id integer," +
            " collection_id integer," +
            " title text," +
            " value text," +
            " delay integer," +
            " path text," +
            " file_name text" +
            ");";
    private final String CREATE_TABLE_COLOR_IDS = "CREATE TABLE IF NOT EXISTS " + TypeTable.COLOR_IDS_TABLE + " (" +
            " id_animated_color integer," +
            " id_color integer" +
            ");";

    public AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_COLLECTION);
        db.execSQL(CREATE_TABLE_COLOR);
        db.execSQL(CREATE_TABLE_COLOR_IDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "The database has been updated! Old version=" + oldVersion + " New version=" + newVersion);
        this.onCreate(db);
    }

    public static void clearTable(final Context context, TypeTable typeTable) {
        Log.i(TAG, "clearTable(), table: " + typeTable.getStringValue());
        try {
            getInstance(context).getWritableDatabase().delete(typeTable.getStringValue(), null, null);
        } catch (Exception e) {
            Log.e(TAG, "Exception, clearTable: ", e);
        }
    }

    public static void clearAllTables(final Context context) {
        Log.i(TAG, "clearAllTables()");
        clearTable(context, TypeTable.COLLECTION_TABLE);
        clearTable(context, TypeTable.COLOR_TABLE);
        clearTable(context, TypeTable.COLOR_IDS_TABLE);
    }

    public static void dropAllTables(final Context context) {
        Log.i(TAG, "dropAllTables()");
        try {
            getInstance(context).getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + TypeTable.COLLECTION_TABLE.getStringValue());
            getInstance(context).getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + TypeTable.COLOR_TABLE.getStringValue());
            getInstance(context).getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + TypeTable.COLOR_IDS_TABLE.getStringValue());
        } catch (Exception e) {
            Log.e(TAG, "Exception, clearTable: ", e);
        }
    }

    public static synchronized void insertCollection(final Context context, FanCollection collection) {
        Log.i(TAG, "insertCollection(), collection: " + collection);
        try {
            ContentValues values = new ContentValues();
            values.put("id", collection.getId());
            values.put("title", collection.getTitle());
            for (FanColor color : collection.getColorList()) {
                insertFanColor(context, color);
            }
            getInstance(context).getWritableDatabase().insert(TypeTable.COLLECTION_TABLE.getStringValue(), null, values);
        }catch (Exception e) {
            Log.e(TAG, "insertCollection(), Exception: " + collection, e);
        }
    }

    public static synchronized void insertFanColor(final Context context, FanColor color) {
        Log.i(TAG, "insertFanColor(), color: " + color);
        try {
            ContentValues values = new ContentValues();
            values.put("id", color.getId());
            values.put("collection_id", color.getCollectionId());
            values.put("title",   color.getTitle());
            if (color instanceof AnimatedColor) {
                values.put("delay", ((AnimatedColor) color).getDelay());
                for (Integer id : ((AnimatedColor) color).getIds()) {
                    insertIds(context, color.getId(), id);
                }
            } else if (color instanceof SolidColor) {
                values.put("value", ((SolidColor) color).getValue());
            } else if (color instanceof FanImage) {
                values.put("path", ((FanImage) color).getPath());
                values.put("file_name", ((FanImage) color).getFileName());
            }
            getInstance(context).getWritableDatabase().insert(TypeTable.COLOR_TABLE.getStringValue(), null, values);
        }catch (Exception e) {
            Log.e(TAG, "insertFanColor(), Exception: ", e);
        }
    }

    public static synchronized void insertIds(final Context context, int animatedColorId, int colorId) {
        Log.i(TAG, "insertIds(), animatedColorId: " + animatedColorId + ", colorId: " + colorId);
        try {
            ContentValues values = new ContentValues();
            values.put("id_animated_color", animatedColorId);
            values.put("id_color", colorId);
            getInstance(context).getWritableDatabase().insert(TypeTable.COLOR_IDS_TABLE.getStringValue(), null, values);
        }catch (Exception e) {
            Log.e(TAG, "insertIds(), Exception:", e);
        }
    }

    public static synchronized List<FanCollection> getCollectionList(final Context context) {
        Log.i(TAG, "getCollectionList()");
        List<FanCollection> result = new ArrayList<>();

        String query = "SELECT  * FROM " + TypeTable.COLLECTION_TABLE.getStringValue();
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex("id");
            if (index >= 0) {
                int id = cursor.getInt(index);
                index = cursor.getColumnIndex("title");
                if (!cursor.isNull(index)) {
                    String title = cursor.getString(index);
                    result.add(new FanCollection(id, title, getColorList(context, id)));
                }
            }
        }
        cursor.close();
        Log.i(TAG, "getCollectionList(), result: " + result);
        return result;
    }

    public static synchronized List<FanColor> getColorList(final Context context, int collectionId) {
        Log.i(TAG, "getColorList(), collectionId: " + collectionId);
        List<FanColor> result = new ArrayList<>();
        String query = "SELECT  * FROM " + TypeTable.COLOR_TABLE.getStringValue() + " WHERE collection_id=" + collectionId;
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex("id");
            if (index >= 0) {
                int id = cursor.getInt(index);
                index = cursor.getColumnIndex("title");
                if (!cursor.isNull(index)) {
                    String title = cursor.getString(index);
                    index = cursor.getColumnIndex("value");
                    if (!cursor.isNull(index)) {
                        String value = cursor.getString(index);
                        result.add(new SolidColor(collectionId, id, title, value));
                        continue;
                    }

                    index = cursor.getColumnIndex("path");
                    if (!cursor.isNull(index)) {
                        String path = cursor.getString(index);
                        index = cursor.getColumnIndex("file_name");
                        if (!cursor.isNull(index)) {
                            String fileName = cursor.getString(index);
                            result.add(new FanImage(collectionId, id, title, path, fileName));
                            continue;
                        }
                    }

                    index = cursor.getColumnIndex("delay");
                    if (index >= 0) {
                        int delay = cursor.getInt(index);
                        if (delay > 0) {
                            List<Integer> ids = getIds(context, id);
                            result.add(new AnimatedColor(collectionId, id, title, ids, delay));
                            continue;
                        }
                    }

                }
            }
        }
        cursor.close();
        Log.i(TAG, "getColorList(), result: " + result);
        return result;
    }

    public static synchronized List<Integer> getIds(final Context context, int animatedColorId) {
        Log.i(TAG, "getIds(), animatedColorId: " + animatedColorId);
        List<Integer> result = new ArrayList<>();
        String query = "SELECT  * FROM " + TypeTable.COLOR_IDS_TABLE.getStringValue() + " WHERE id_animated_color=" + animatedColorId;
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex("id_color");
            if (index >= 0) {
                result.add(cursor.getInt(index));
            }
        }
        cursor.close();
        Log.i(TAG, "getIds(), result: " + result);
        return result;
    }

    public static synchronized FanColor getSolidColor(final Context context, int id) {
        Log.i(TAG, "getSolidColor(), id: " + id);
        FanColor result = null;
        String query = "SELECT  * FROM " + TypeTable.COLOR_TABLE.getStringValue() + " WHERE collection_id=" + SOLID_COLOR + " AND id=" + id;
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex("title");
        if (!cursor.isNull(index)) {
            String title = cursor.getString(index);
            index = cursor.getColumnIndex("value");
            if (!cursor.isNull(index)) {
                String value = cursor.getString(index);
                result = new SolidColor(SOLID_COLOR, id, title, value);
            }
        }
        cursor.close();
        Log.i(TAG, "getSolidColor(), result: " + result);
        return result;
    }

    public static synchronized FanColor getAnimatedColor(final Context context, int animatedColorId) {
        Log.i(TAG, "getAnimatedColor(), id: " + animatedColorId);
        FanColor result = null;
        String query = "SELECT  * FROM " + TypeTable.COLOR_TABLE.getStringValue() + " WHERE collection_id=" + ANIMATED_COLOR + " AND id=" + animatedColorId;
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex("title");
        if (!cursor.isNull(index)) {
            String title = cursor.getString(index);
            index = cursor.getColumnIndex("delay");
            if (index >= 0) {
                int delay = cursor.getInt(index);
                if (delay > 0) {
                    List<Integer> ids = getIds(context, animatedColorId);
                    result = new AnimatedColor(ANIMATED_COLOR, animatedColorId, title, ids, delay);

                }
            }
        }
        cursor.close();
        Log.i(TAG, "getAnimatedColor(), result: " + result);
        return result;
    }

    public static synchronized FanColor getFanColor(final Context context, int collectionId, int id) {
        Log.i(TAG, "getFanColor(), id: " + id);
        switch (collectionId) {
            case SOLID_COLOR:
                return getSolidColor(context, id);
            case ANIMATED_COLOR:
                return getAnimatedColor(context, id);
            default:
                return null;
        }

    }

}
