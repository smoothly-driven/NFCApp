package com.example.robot_server.nfcapp.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import static android.content.Context.TELEPHONY_SERVICE;

public class Utils {

    @SuppressWarnings("all")
    public static String getDeviceImei(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

}
