package com.example.robot_server.nfcapp.processors;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

import com.example.robot_server.nfcapp.domain.ScanResult;
import com.example.robot_server.nfcapp.utils.NfcUtils;

class MetaProcessor implements IntentProcessor {

    @Override
    public void process(Intent intent, ScanResult.ScanResultBuilder builder) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        builder.cardTechnology(tag.getTechList())
                .cardUid(NfcUtils.byteArrayToHex(tag.getId()));
    }
}
