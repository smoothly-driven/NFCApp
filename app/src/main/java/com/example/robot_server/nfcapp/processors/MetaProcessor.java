package com.example.robot_server.nfcapp.processors;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

import com.example.robot_server.nfcapp.utils.NfcUtils;
import com.example.robot_server.nfcapp.ScanResult;

/**
 * Created by robot-server on 13.03.17.
 */

public class MetaProcessor extends IntentProcessor {

    public static final int ID = 0;

    public MetaProcessor() {
        super(ID);
    }

    @Override
    public void process(Intent intent, ScanResult.ScanResultBuilder builder) {
        Log.v("NFCTAG", intent.getAction());
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        builder.cardTechnology(tag.getTechList())
                .cardUid(NfcUtils.byteArrayToHex(tag.getId()));
    }
}
