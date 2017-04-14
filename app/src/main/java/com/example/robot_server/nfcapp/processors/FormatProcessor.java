package com.example.robot_server.nfcapp.processors;

import android.content.Intent;

import com.example.robot_server.nfcapp.annotations.After;
import com.example.robot_server.nfcapp.annotations.Before;
import com.example.robot_server.nfcapp.annotations.Inject;
import com.example.robot_server.nfcapp.domain.ScanResult;

@Before(IntentProcessor.WRITE)
@After(IntentProcessor.READ)
public class FormatProcessor implements IntentProcessor {

    @Inject(name = "formatting", nullable = false)
    private String formatting;

    public FormatProcessor() {
        formatting = null; // just to shut Lint up.
    }

    @Override
    public void process(Intent intent, ScanResult.ScanResultBuilder builder) {

    }
}
