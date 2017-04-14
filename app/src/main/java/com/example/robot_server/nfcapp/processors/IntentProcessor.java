package com.example.robot_server.nfcapp.processors;

import android.content.Intent;

import com.example.robot_server.nfcapp.domain.ScanResult;

public interface IntentProcessor {

    String META = "meta";
    String READ = "read";
    String WRITE = "write";
    String FORMAT = "format";

    void process(Intent intent, ScanResult.ScanResultBuilder builder);

}
