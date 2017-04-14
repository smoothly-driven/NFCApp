package com.example.robot_server.nfcapp;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppContext {

    private static final String PROPS_FILE = "properties.props";
    private static Properties props;

    public static void loadContext(Context context) {
        props = new Properties();
        try (InputStream is = context.getAssets().open(PROPS_FILE)) {
            props.load(is);
            Log.d("NFCTAG", "Properties loaded.");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("NFCTAG", "Properties failed to load.");
        }
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }
}
