package com.example.robot_server.nfcapp;

import android.nfc.NdefRecord;

import java.io.UnsupportedEncodingException;

/**
 * Created by smoothoverdrive on 2/15/2017.
 */

public class NFCUtils {

    public static String readNdefRecord(NdefRecord record) {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */
        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        try {
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
