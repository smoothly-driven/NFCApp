package com.example.robot_server.nfcapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
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

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String SERVER_IP = "http://10.111.17.139:5000";

    private OkHttpClient mHttpClient;
    private NfcAdapter mNfcAdapter;
    private TextView mTagContentsTextView;
    private EditText mWriteToTagEditText;
    private CheckBox mShouldWriteCheckBox;

    private String mCardContents = "";
    private String editTextContents = "";

    private String mCardTechnology = "Ndef";
    private long mScanDuration;
    private int mScans;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHttpClient = new OkHttpClient();
        mTagContentsTextView = (TextView) findViewById(R.id.textView);
        mWriteToTagEditText = (EditText) findViewById(R.id.editText);
        mShouldWriteCheckBox = (CheckBox) findViewById(R.id.checkBox);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            Log.e("NFCTAG", "The device doesn't have NFC");
            finish();
            return;
        }

        Log.d("WHAT", "Serial : " + Build.SERIAL);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        long intentReceptionTime = SystemClock.elapsedRealtime(); //apparently better than System.currentTimeMillis() ?
        handleIntent(intent);
        mScanDuration = SystemClock.elapsedRealtime() - intentReceptionTime;
        String prefixString = mShouldWriteCheckBox.isChecked() ? "Reading + writing took " : "Reading took ";
        Toast.makeText(this, prefixString + mScanDuration + " ms.", Toast.LENGTH_SHORT).show();

        try {
            postScanResult(mCardTechnology, mCardContents, mScanDuration, mScans);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        updateUi();

    }

    private void handleIntent(Intent intent) {

        Log.d("NFCTAG", intent.getAction());
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (mShouldWriteCheckBox.isChecked()) {
            int opStatus = NFCUtils.writeTag(NFCUtils.getMessageAsNdef(mWriteToTagEditText.getText().toString()), detectedTag);
            Log.d("NFCTAG", "writing operation returned a code " + opStatus);
            if (opStatus == NFCUtils.CODE_SUCCESS) {
                mCardContents = mWriteToTagEditText.getText().toString();
                editTextContents = "";
            }
        } else {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                }

                mCardContents = NFCUtils.readMessageContents(messages);
                Log.d("NFCTAG", "messages were successfully decoded : " + mCardContents);
                editTextContents = mWriteToTagEditText.getText().toString();
            }
        }
        mScans++;
    }

    private void updateUi() {
        mTagContentsTextView.setText(mCardContents);
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

    void postScanResult(String cardTechnology, String cardContents, long scanDuration, int scans) throws IOException {

        RequestBody body = new FormBody.Builder()
                .add("card_technology", cardTechnology)
                .add("card_contents", cardContents)
                .add("scan_duration", String.valueOf(scanDuration))
                .add("scans", String.valueOf(scans))
                .add("device_serial", Build.SERIAL)
                .build();

        Request request = new Request.Builder()
                .url(SERVER_IP + "/results")
                .post(body)
                .build();

        mHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("NFCTAG", "exception : ");
                        e.printStackTrace();
                        Log.d("NFCTAG", "Error posting scan result!");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d("NFCTAG", "Successfully posted scan result!");
                        Log.d("NFCTAG", response.body().string());
                    }
                });
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
