package party.danyang.doodles.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Mr_Wrong on 16/1/15.
 */
public class PreferencesHelper {
    static volatile SharedPreferences singleton = null;

    public static SharedPreferences getInstance(Context context) {
        if (singleton == null) {
            synchronized (SharedPreferences.class) {
                if (singleton == null) {
                    singleton = PreferenceManager.getDefaultSharedPreferences(context);
                }
            }
        }
        return singleton;
    }

    public static final String PREF_LOAD_FROM_CACHE_FIRST = "pref.load_from_cache_first";

    public static void setLoadFromCacheFirst(Context context, boolean b) {
        getInstance(context).edit().putBoolean(PREF_LOAD_FROM_CACHE_FIRST, b).apply();
    }

    public static boolean getLoadFromCacheFirst(Context context) {
        return getInstance(context).getBoolean(PREF_LOAD_FROM_CACHE_FIRST, false);
    }
}
