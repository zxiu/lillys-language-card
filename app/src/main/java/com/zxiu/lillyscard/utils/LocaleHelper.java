package com.zxiu.lillyscard.utils;

/**
 * Created by Xiu on 10/26/2016.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.lang3.LocaleUtils;

import java.util.Locale;

/**
 * This class is used to change your application locale and persist this change for the next time
 * that your app is going to be used.
 * <p/>
 * You can also change the locale of your application on the fly by using the setLocale method.
 * <p/>
 * Created by gunhansancar on 07/10/15.
 */
public class LocaleHelper {

    private static final String SELECTED_LOCALE = "Locale.Helper.Selected.LOCALE";

    public static void init(Context context) {
        setLocale(context, getPersistedLocale(context, Locale.getDefault()));
    }

    public static void init(Context context, Locale defaultLocale) {
        setLocale(context, getPersistedLocale(context, defaultLocale));
    }

    public static Locale getLocale(Context context) {
        return getPersistedLocale(context, Locale.getDefault());
    }

    public static void setLocale(Context context, Locale locale) {
        persist(context, locale);
        updateResources(context, locale);
    }

    private static Locale getPersistedLocale(Context context, Locale defaultLocale) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return LocaleUtils.toLocale(preferences.getString(SELECTED_LOCALE, defaultLocale.toString()));
    }

    private static void persist(Context context, Locale locale) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LOCALE, locale.toString());
        editor.apply();
    }

    private static void updateResources(Context context, Locale locale) {
        Log.i("LocaleHelper", "updateResources locale="+locale);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Configuration config = context.getResources().getConfiguration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            configuration.setLocale(locale);
//        } else {
//            configuration.locale = locale;
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            context.createConfigurationContext(configuration);
//        } else {
//            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
//        }
    }
}