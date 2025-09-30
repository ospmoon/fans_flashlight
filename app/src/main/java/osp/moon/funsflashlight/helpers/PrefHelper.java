package osp.moon.funsflashlight.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class PrefHelper {

    /**
     * The names(keys) of the preference
     */
    public static final String COLLECTION_ID_KEY = "osp.moon.funsflashlight.COLLECTION_ID_KEY";
    public static final String COLOR_ID_KEY = "osp.moon.funsflashlight.COLOR_ID_KEY";

    /**
     * Gets a SharedPreferences instance that points to the default file
     * that is used by the preference framework in the given context.
     *
     * @param context Interface to global information about an application environment
     * @return SharedPreferences instance
     */
    private static SharedPreferences getPref(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Stores the current collectionId in the SharedPreferences
     *
     * @param context Interface to global information about an application environment
     * @param collectionId The current collectionId
     */
    public static void storeCollectionId(final Context context, final int collectionId) {
        getPref(context).edit().putInt(COLLECTION_ID_KEY, collectionId).apply();
    }

    /**
     * Reads the current collectionId from the SharedPreferences
     *
     * @param context Interface to global information about an application environment
     * @return The last saved collectionId
     */
    public static int readLastSavedCollectionId(final Context context) {
        return getPref(context).getInt(COLLECTION_ID_KEY, -1);
    }

    /**
     * Stores the current colorId in the SharedPreferences
     *
     * @param context Interface to global information about an application environment
     * @param colorId The current color Id
     */
    public static void storeColorId(final Context context, final int colorId) {
        getPref(context).edit().putInt(COLOR_ID_KEY, colorId).apply();
    }

    /**
     * Reads the last saved color Id from the SharedPreferences
     *
     * @param context Interface to global information about an application environment
     * @return The last saved color Id
     */
    public static int readLastSavedColorId(final Context context) {
        return getPref(context).getInt(COLOR_ID_KEY, -1);
    }
}
