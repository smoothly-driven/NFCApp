package com.example.robot_server.nfcapp.utils;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * This is all code found on the Internet. Should probably tidy it up.
 */
public class NfcUtils {

    public static final int CODE_SUCCESS = 0;
    private static final int CODE_FAILURE = 1;

    public static String readMessageContents(NdefMessage[] messages) {
        String text = "";
        for (NdefMessage message : messages) {
            for (NdefRecord record : message.getRecords()) {
                text += payloadToString(record.getPayload());
            }
        }
        return text;
    }

    private static String payloadToString(byte[] payload) {
        byte status = payload[0];
        int enc = status & 0x80; // Bit mask 7th bit 1
        String encString;
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
        byte[] messageBytes = message.getBytes(Charset.forName("UTF-8"));
        byte[] payload = new byte[messageBytes.length + 1];       //add 1 for the URI Prefix
        payload[0] = 0x00;
        System.arraycopy(messageBytes, 0, payload, 1, messageBytes.length);
        NdefRecord rtdUriRecord = new NdefRecord(
                NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        return new NdefMessage(new NdefRecord[]{rtdUriRecord});
    }

    @SuppressWarnings("all")
    public static String byteArrayToHex(byte[] inarray) {
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (int j = 0; j < inarray.length; j++) {
            int in = (int) inarray[j] & 0xff;
            int i = (in >> 4) & 0x0f;
            out += hex[i];
        }
        return out;
    }
}
