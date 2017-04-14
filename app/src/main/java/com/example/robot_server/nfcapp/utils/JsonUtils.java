package com.example.robot_server.nfcapp.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtils {

    public static Object getNested(JSONObject obj, String key) {
        String[] keys = key.split("\\.|/"); // accept . and / as valid delimiters
        Object value = obj;
        for (String s : keys) {
            value = ((JSONObject) value).opt(s);
            if (value == null) return null;
        }
        return value;
    }

    public static boolean arrayContains(JSONArray array, Object obj) {
        for (int i = 0; i < array.length(); i++) {
            if (obj.equals(array.opt(i))) return true;
        }
        return false;
    }

    public static void removeFromArray(JSONArray array, String value) {
        for (int i = 0; i < array.length(); i++) {
            if (value.equals(array.opt(i))) {
                array.remove(i);
            }
        }
    }
}
