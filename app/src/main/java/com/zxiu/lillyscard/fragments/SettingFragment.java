package com.zxiu.lillyscard.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;
import com.zxiu.lillyscard.R;
import com.zxiu.lillyscard.utils.LocaleHelper;
import com.zxiu.lillyscard.utils.MediaManager;
import com.zxiu.lillyscard.utils.SettingManager;

import org.apache.commons.lang3.LocaleUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Xiu on 10/27/2016.
 */

public class SettingFragment extends PreferenceFragmentCompatDividers {
    ListPreference preferenceLangCard;
    SwitchPreferenceCompat speechPreference, musicPreference;
    List<SettingChangedListener> settingChangedListeners = new ArrayList<>();
    static final Locale[] supportLocales = {Locale.ENGLISH, Locale.GERMAN, Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE, Locale.ITALIAN, Locale.FRENCH, Locale.JAPANESE, Locale.KOREAN};
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
        initPreferences();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(Color.parseColor("#80ffffff"));
    }

    protected void initPreferences() {
        Locale[] locales = supportLocales;
        String[] localeValues = new String[locales.length];
        String[] localeNames = new String[locales.length];
        for (int i = 0; i < locales.length; i++) {
            localeValues[i] = locales[i].toString();
            localeNames[i] = getLocaleDisplayName(locales[i]);
        }
        preferenceLangCard = (ListPreference) findPreference(getString(R.string.key_game_language));
        preferenceLangCard.setEntryValues(localeValues);
        preferenceLangCard.setEntries(localeNames);
        preferenceLangCard.setSummary(getLocaleDisplayName(LocaleHelper.getLocale(getActivity())));
        preferenceLangCard.setDefaultValue(LocaleHelper.getLocale(getActivity()).toString());
        preferenceLangCard.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Locale locale = LocaleUtils.toLocale((String) o);
                LocaleHelper.setLocale(getActivity(), locale);
                preference.setSummary(getLocaleDisplayName(locale));
                return true;
            }
        });

        speechPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.key_speech));
        speechPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SettingManager.putValue(preference.getKey(), newValue);
                return true;
            }
        });

        musicPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.key_music));
        musicPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    SettingManager.putValue(preference.getKey(), newValue);
                    if ((Boolean) newValue) {
                        MediaManager.getInstance().resume();
                    } else {
                        MediaManager.getInstance().pause();
                    }
                }
                return true;
            }
        });
    }

    private String getLocaleDisplayName(Locale locale) {
        String displayName = locale.getDisplayLanguage(locale);
        if (locale.getDisplayCountry(locale) != null) {
            String ext = new String();
            if (locale.equals(Locale.SIMPLIFIED_CHINESE)) {
                ext = "简体";
            } else if (locale.equals(Locale.TRADITIONAL_CHINESE)) {
                ext = "繁體";
            } else {
                ext = locale.getDisplayCountry(locale);
            }
            if (!ext.isEmpty()) {
                displayName += "(" + ext + ")";
            }
        }
        return displayName;
    }

    public interface SettingChangedListener {
        public void onChange(int key, Object value);
    }

    public void addSettingChangedListener(SettingChangedListener listener) {
        settingChangedListeners.add(listener);
    }

    public void remmoveSettingChangedListener(SettingChangedListener listener) {
        settingChangedListeners.remove(listener);
    }
}

