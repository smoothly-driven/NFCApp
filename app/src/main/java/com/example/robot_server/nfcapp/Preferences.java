package com.example.robot_server.nfcapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Set;

/**
 * Created by robot-server on 09.03.17.
 */

public class Preferences {

    public static final String PREFERENCES_NAME = "nfc_preferences";

    public static void putStringSet(Context context, String key, Set<String> strings) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet(key, strings);
        editor.apply();
    }

    public static Set<String> getStringSet(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, 0);
        return sp.getStringSet(key, null);
    }
}
