package com.example.robot_server.nfcapp.processors;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

import com.example.robot_server.nfcapp.domain.ScanResult;
import com.example.robot_server.nfcapp.domain.StringWrapper;
import com.example.robot_server.nfcapp.utils.NfcUtils;

/**
 * Created by robot-server on 13.03.17.
 */

class WriteProcessor extends IntentProcessor {

    public static final int ID = IntentProcessor.WRITE;

    private StringWrapper mText;

    public WriteProcessor() {
        super(ID);
    }

    public WriteProcessor(StringWrapper text) {
        super(ID);
        mText = text;
    }

    @Override
    public void process(Intent intent, ScanResult.ScanResultBuilder builder) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        int opStatus = NfcUtils.writeTag(NfcUtils.getMessageAsNdef(mText.get()), tag);
        Log.v("NFCTAG", "writing operation returned a code " + opStatus);
        if (opStatus == NfcUtils.CODE_SUCCESS) {
            builder.cardContent(mText.get());
        }
    }

    @Override
    public void receive(Object... args) {
        mText = (StringWrapper) args[0];
    }
}
