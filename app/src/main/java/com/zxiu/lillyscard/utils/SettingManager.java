package com.zxiu.lillyscard.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.zxiu.lillyscard.App;

import java.util.Set;

/**
 * Created by Xiu on 10/20/2016.
 */

public class SettingManager {

    public static void putValue(String key, Object value) {
        if (value instanceof Integer) {
            getSharedPreferences().edit().putInt(key, (Integer) value).apply();
        } else if (value instanceof String) {
            getSharedPreferences().edit().putString(key, (String) value).apply();
        } else if (value instanceof Boolean) {
            getSharedPreferences().edit().putBoolean(key, (Boolean) value).apply();
        } else if (value instanceof Float) {
            getSharedPreferences().edit().putFloat(key, (Float) value).apply();
        } else if (value instanceof Set) {
            getSharedPreferences().edit().putStringSet(key, (Set<String>) value).apply();
        }
    }

    public static Object getValue(String key, Object defaultValue) {
        if (defaultValue instanceof Integer) {
            return getSharedPreferences().getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof String) {
            return getSharedPreferences().getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return getSharedPreferences().getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            return getSharedPreferences().getFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof Set) {
            return getSharedPreferences().getStringSet(key, (Set<String>) defaultValue);
        }
        return null;
    }

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(App.context);
    }

}
