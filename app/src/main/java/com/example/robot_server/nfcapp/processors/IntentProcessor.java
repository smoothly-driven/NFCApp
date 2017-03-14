package com.example.robot_server.nfcapp.processors;

import android.content.Intent;

import com.example.robot_server.nfcapp.ScanResult;

/**
 * Created by robot-server on 13.03.17.
 */

public abstract class IntentProcessor {

    public static final int ID = 0;

    private int mId;

    public IntentProcessor(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public abstract void process(Intent intent, ScanResult.ScanResultBuilder builder);

    public void receive(Object... args) {

    }
}
