package com.example.robot_server.nfcapp.processors;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

import com.example.robot_server.nfcapp.annotations.Inject;
import com.example.robot_server.nfcapp.domain.ScanResult;
import com.example.robot_server.nfcapp.domain.StringWrapper;
import com.example.robot_server.nfcapp.utils.NfcUtils;

class WriteProcessor extends IntentProcessor {

    private static final int ID = IntentProcessor.WRITE;

    @Inject(value="toWrite", nullable=false)
    private StringWrapper mTextToWrite;

    public WriteProcessor() {
        super(ID);
    }

    @Override
    public void process(Intent intent, ScanResult.ScanResultBuilder builder) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        int opStatus = NfcUtils.writeTag(NfcUtils.getMessageAsNdef(mTextToWrite.get()), tag);
        Log.v("NFCTAG", "writing operation returned a code " + opStatus);
        if (opStatus == NfcUtils.CODE_SUCCESS) {
            builder.cardContent(mTextToWrite.get());
        }
    }

    @Override
    public void receive(Object... args) {
        mTextToWrite = (StringWrapper) args[0];
    }
}
