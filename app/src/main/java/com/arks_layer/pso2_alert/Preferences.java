package com.arks_layer.pso2_alert;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private static final String USER_PREFS = "PSO2AlertPrefs";
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    public Preferences(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(USER_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }

    public int getInt(String intKeyValue, int _default) {
        return appSharedPrefs.getInt(intKeyValue, _default);
    }

    public String getString(String stringKeyValue, String _default) {
        return appSharedPrefs.getString(stringKeyValue, _default);
    }

    public Boolean getBoolean(String stringKeyValue, boolean _default) {
        return appSharedPrefs.getBoolean(stringKeyValue, _default);
    }

    public void putInt(String intKeyValue, int _intValue) {

        prefsEditor.putInt(intKeyValue, _intValue).commit();
    }

    public void putString(String stringKeyValue, String _stringValue) {
        prefsEditor.putString(stringKeyValue, _stringValue).commit();
    }

    public void putBoolean(String stringKeyValue, Boolean _bool) {
        prefsEditor.putBoolean(stringKeyValue, _bool).commit();
    }

    public void clearData() {
        prefsEditor.clear().commit();
    }
}
