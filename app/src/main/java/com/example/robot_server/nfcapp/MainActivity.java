package com.example.robot_server.nfcapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity {

    public static final String MIME_TEXT_PLAIN = "text/plain";

    private NfcAdapter mNfcAdapter;
    private TextView mTagContentsTextView;
    private EditText mWriteToTagEditText;
    private CheckBox mShouldWriteCheckBox;

    private String tagContents = "";
    private String editTextContents = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTagContentsTextView = (TextView) findViewById(R.id.textView);
        mWriteToTagEditText = (EditText) findViewById(R.id.editText);
        mShouldWriteCheckBox = (CheckBox) findViewById(R.id.checkBox);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            Log.e("NFCTAG", "The device doesn't have NFC");
            finish();
            return;
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        long intentReceptionTime = SystemClock.elapsedRealtime(); //apparently better than System.currentTimeMillis() ?
        handleIntent(intent);
        long processDuration = SystemClock.elapsedRealtime() - intentReceptionTime;
        String prefixString = mShouldWriteCheckBox.isChecked() ? "Reading + writing took " : "Reading took ";
        Toast.makeText(this, prefixString + processDuration + " ms.", Toast.LENGTH_SHORT).show();
        updateUi();

    }

    private void handleIntent(Intent intent) {

        Log.d("NFCTAG", intent.getAction());
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (mShouldWriteCheckBox.isChecked()) {
            int opStatus = NFCUtils.writeTag(NFCUtils.getMessageAsNdef(mWriteToTagEditText.getText().toString()), detectedTag);
            Log.d("NFCTAG", "writing operation returned a code " + opStatus);
            if (opStatus == NFCUtils.CODE_SUCCESS) {
                tagContents = mWriteToTagEditText.getText().toString();
                editTextContents = "";
            }
        } else {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                }

                tagContents = NFCUtils.readMessageContents(messages);
                Log.d("NFCTAG", "messages were successfully decoded : " + tagContents);
                editTextContents = mWriteToTagEditText.getText().toString();
            }
        }
    }

    private void updateUi() {
        mTagContentsTextView.setText(tagContents);
        mWriteToTagEditText.setText(editTextContents);
        mShouldWriteCheckBox.setChecked(false);
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
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

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
