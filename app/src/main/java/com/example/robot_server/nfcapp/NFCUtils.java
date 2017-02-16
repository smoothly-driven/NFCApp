package com.example.robot_server.nfcapp;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by robot-server on 14.02.17.
 */

public class NFCUtils {

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FAILURE = 1;



    public static String readMessageContents(NdefMessage[] messages) {
        String text = "";
        for (NdefMessage message : messages) {
            for (NdefRecord record : message.getRecords()) {
                text += payloadToString(record.getPayload());
            }
        }
        return text;
    }

    public static String payloadToString(byte[] payload) {
        byte status = payload[0];
        int enc = status & 0x80; // Bit mask 7th bit 1
        String encString = null;
        if (enc == 0) {
            encString = "UTF-8";
        } else {
            encString = "UTF-16";
        }

        int ianaLength = status & 0x3F; // Bit mask bit 5..0

        try {
            return new String(payload, ianaLength + 1,
                    payload.length - 1 - ianaLength, encString);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static int writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        String mess = "";
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable() || ndef.getMaxSize() < size) {
                    return CODE_FAILURE;
                }
                ndef.writeNdefMessage(message);
                return CODE_SUCCESS;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return CODE_SUCCESS;
                    } catch (IOException e) {
                        return CODE_FAILURE;
                    }
                } else {
                    return CODE_FAILURE;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CODE_FAILURE;
        }
    }

    public static NdefMessage getMessageAsNdef(String message) {
        boolean addAAR = false;
        byte[] messageBytes = message.getBytes(Charset.forName("UTF-8"));
        byte[] payload = new byte[messageBytes.length + 1];       //add 1 for the URI Prefix
        payload[0] = 0x00;
        System.arraycopy(messageBytes, 0, payload, 1, messageBytes.length);
        NdefRecord rtdUriRecord = new NdefRecord(
                NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        if (addAAR) {
            // note: returns AAR for different app (nfcreadtag)
            return new NdefMessage(new NdefRecord[]{
                    rtdUriRecord, NdefRecord.createApplicationRecord("com.tapwise.nfcreadtag")
            });
        } else {
            return new NdefMessage(new NdefRecord[]{
                    rtdUriRecord});
        }
    }

}