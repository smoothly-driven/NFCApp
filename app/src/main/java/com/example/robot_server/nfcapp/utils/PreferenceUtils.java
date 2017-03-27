package com.example.robot_server.nfcapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.robot_server.nfcapp.domain.Server;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by robot-server on 09.03.17.
 */

public class PreferenceUtils {

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

    public static Set<String> serversToStringSet(Set<Server> servers) {
        Set<String> strings = new HashSet<>();
        for (Server server : servers) {
            strings.add(server.toString());
        }
        return strings;
    }

    public static Set<Server> stringsToServerSet(Set<String> strings) {
        if (strings == null) return null;
        Set<Server> servers = new HashSet<>();
        for (String string : strings) {
            servers.add(Server.fromJsonString(string));
        }
        return servers;
    }
}
