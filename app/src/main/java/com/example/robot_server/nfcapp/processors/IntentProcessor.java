package com.example.robot_server.nfcapp.processors;

import android.content.Intent;

import com.example.robot_server.nfcapp.domain.ScanResult;

public abstract class IntentProcessor {

    public static final int META = 0;
    public static final int READ = 1;
    public static final int WRITE = 2;

    private int mId;

    IntentProcessor(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public abstract void process(Intent intent, ScanResult.ScanResultBuilder builder);

}
