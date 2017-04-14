package com.example.robot_server.nfcapp.processors;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

import com.example.robot_server.nfcapp.domain.ScanResult;
import com.example.robot_server.nfcapp.utils.NfcUtils;

class ReadProcessor implements IntentProcessor {

    @Override
    public void process(Intent intent, ScanResult.ScanResultBuilder builder) {
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMessages != null) {
            NdefMessage[] messages = new NdefMessage[rawMessages.length];
            for (int i = 0; i < rawMessages.length; i++) {
                messages[i] = (NdefMessage) rawMessages[i];
            }
            builder.cardContent(NfcUtils.readMessageContents(messages));
        }
    }

}
