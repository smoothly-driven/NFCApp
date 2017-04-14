package com.example.robot_server.nfcapp.processors;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

import com.example.robot_server.nfcapp.annotations.After;
import com.example.robot_server.nfcapp.annotations.Inject;
import com.example.robot_server.nfcapp.domain.ScanResult;
import com.example.robot_server.nfcapp.utils.NfcUtils;

@After(IntentProcessor.READ)
class WriteProcessor implements IntentProcessor {

    @Inject(name = "toWrite", nullable = false)
    private String mToWrite;

    public WriteProcessor() {
        mToWrite = null; //just to shut Lint up.
    }

    @Override
    public void process(Intent intent, ScanResult.ScanResultBuilder builder) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        int opStatus = NfcUtils.writeTag(NfcUtils.getMessageAsNdef(mToWrite), tag);
        if (opStatus == NfcUtils.CODE_SUCCESS) {
            builder.cardContent(mToWrite);
        }
    }

}
