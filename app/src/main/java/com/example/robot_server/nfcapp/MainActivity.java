package com.example.robot_server.nfcapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    public static final String MIME_TEXT_PLAIN = "text/plain";

    private NfcAdapter mNfcAdapter;
    private TextView mTagContentsTextView;
    private EditText mTextToWriteEditText;
    private CheckBox mShouldWriteCheckBox;
    private String mTagContents;
    private String mTextToWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTagContentsTextView = (TextView) findViewById(R.id.tv_tag_contents);
        mTextToWriteEditText = (EditText) findViewById(R.id.et_text_to_write);
        mShouldWriteCheckBox = (CheckBox) findViewById(R.id.cb_write_to_tag);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            Log.e("NFCTAG", "The device doesn't have NFC");
            finish();
            return;
        }

        if (mNfcAdapter.isEnabled()) {
            Log.i("NFCTAG", "NFC is enabled");
        } else {
            Log.i("NFCTAG", "NFC is disabled");
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
        updateUi();
    }

    private void handleIntent(Intent intent) {
        Log.d("NFCTAG", intent.getAction());
        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            handleNdef(tag, mShouldWriteCheckBox.isChecked(), mTextToWriteEditText.getText().toString());
            mShouldWriteCheckBox.setChecked(false);
        }
    }

    private void handleNdef(Tag tag, boolean shouldWrite, String textToWrite) {
        Ndef ndef;
        try {
            ndef = Ndef.get(tag);
            if (ndef == null) return;
            ndef.connect();

            if (shouldWrite && ndef.isWritable()) {
                writeNdefMessage(ndef, textToWrite);
            } else {
                Log.d("NFCTAG", "tag type : " + ndef.getType());
                NdefMessage message = ndef.getCachedNdefMessage();
                if (message == null) {
                    message = ndef.getNdefMessage();
                    if (message == null) {
                        Log.i("NFCTAG", "Empty tag");
                        mTagContents = "";
                        return;
                    }
                }
                mTagContents = "";
                for (NdefRecord record : message.getRecords()) {
                    mTagContents += NFCUtils.readNdefRecord(record);
                }
            }
            ndef.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("NFCTAG", "Could not connect to tag");
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("NFCTAG", "error handling tag");
            return;
        }
    }

    private NdefRecord createNdefRecord(String text) {

        //create the message in according with the standard
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = null;
        try {
            langBytes = lang.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return null;
        }
        int langLength = langBytes.length;
        int textLength = textBytes.length;

        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        return recordNFC;
    }

    private void writeNdefMessage(Ndef ndef, String text) throws IOException, FormatException {
        NdefRecord[] records = {createNdefRecord(text)};
        NdefMessage message = new NdefMessage(records);
        ndef.writeNdefMessage(message);
    }

    private void updateUi() {
        mTagContentsTextView.setText(mTagContents);
        if (mShouldWriteCheckBox.isChecked()) mTextToWriteEditText.setText(mTextToWrite);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[2];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);

        filters[1] = new IntentFilter();
        filters[1].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filters[1].addCategory(Intent.CATEGORY_DEFAULT);
        //try {
        //    filters[0].addDataType(MIME_TEXT_PLAIN);
        //} catch (IntentFilter.MalformedMimeTypeException e) {
        //    throw new RuntimeException("Check your mime type.");
        //}

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting to stop the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

}
