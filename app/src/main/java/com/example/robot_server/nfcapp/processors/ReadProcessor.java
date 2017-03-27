package com.example.robot_server.nfcapp.processors;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.util.Log;

import com.example.robot_server.nfcapp.domain.ScanResult;
import com.example.robot_server.nfcapp.domain.StringWrapper;
import com.example.robot_server.nfcapp.utils.NfcUtils;

/**
 * Created by robot-server on 13.03.17.
 */

class ReadProcessor extends IntentProcessor {

    public static final int ID = IntentProcessor.READ;

    private StringWrapper mString;

    public ReadProcessor() {
        super(ID);
    }

    public ReadProcessor(StringWrapper string) {
        super(ID);
        mString = string;
    }

    @Override
    public void process(Intent intent, ScanResult.ScanResultBuilder builder) {
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMessages != null) {
            NdefMessage[] messages = new NdefMessage[rawMessages.length];
            for (int i = 0; i < rawMessages.length; i++) {
                messages[i] = (NdefMessage) rawMessages[i];
            }
            String cardContents = NfcUtils.readMessageContents(messages);
            builder.cardContent(cardContents);
            if (mString != null) mString.set(cardContents);
            Log.v("NFCTAG", "messages were successfully decoded : " + cardContents);
        }
    }

    @Override
    public void receive(Object... args) {
        mString = (StringWrapper) args[0];
    }
}
