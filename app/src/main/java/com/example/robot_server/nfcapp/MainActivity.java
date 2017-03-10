package com.example.robot_server.nfcapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String PLAIN_TEXT_MEDIA_TYPE = "text/plain";
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    public static final String DEFAULT_SERVER_IP = "http://10.111.17.139:5000";
    public static final String KEY_SERVERS = "servers";

    private Set<String> mServers;
    private OkHttpClient mHttpClient;
    private NfcAdapter mNfcAdapter;
    private TextView mTagContentsTextView;
    private EditText mWriteToTagEditText;
    private CheckBox mShouldWriteCheckBox;

    private String mCardContents = "";
    private String editTextContents = "";

    private int mScans;

    private String mIMEI;
    private String mFamocoId;
    private String mModel;
    private String mImage;

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[2];
        String[][] techList = new String[][]{};

        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);

        filters[1] = new IntentFilter();
        filters[1].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filters[1].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(PLAIN_TEXT_MEDIA_TYPE);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting to stop the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        mServers = Preferences.getStringSet(this, KEY_SERVERS);
        if (mServers == null) {
            mServers = new HashSet<>();
            mServers.add(DEFAULT_SERVER_IP);
        }

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        this.mIMEI = tm.getDeviceId();
        this.mFamocoId = Build.SERIAL;
        this.mImage = Build.DISPLAY;
        this.mModel = Build.MODEL;
    }

    private void addServer(String server) {
        mServers.add(server);
        Preferences.putStringSet(this, KEY_SERVERS, mServers);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ScanResult scanResult = handleIntent(intent);
        try {
            postScanResult(scanResult);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        updateUi();
    }

    private ScanResult handleIntent(Intent intent) {
        long intentReceptionTime = SystemClock.elapsedRealtime(); //apparently better than System.currentTimeMillis() ?
        Log.v("NFCTAG", intent.getAction());
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String[] cardTechnology = detectedTag.getTechList();
        if (mShouldWriteCheckBox.isChecked()) {
            int opStatus = NFCUtils.writeTag(NFCUtils.getMessageAsNdef(mWriteToTagEditText.getText().toString()), detectedTag);
            Log.v("NFCTAG", "writing operation returned a code " + opStatus);
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
                Log.v("NFCTAG", "messages were successfully decoded : " + mCardContents);
                editTextContents = mWriteToTagEditText.getText().toString();
            }
        }
        mScans++;
        long scanDuration = SystemClock.elapsedRealtime() - intentReceptionTime;
        Toast.makeText(this, mShouldWriteCheckBox.isChecked() ? "Reading + writing took " : "Reading took " + scanDuration + " ms.", Toast.LENGTH_SHORT).show();
        return new ScanResult(mCardContents, cardTechnology, mScans, this.mIMEI, this.mFamocoId, this.mModel, this.mImage, scanDuration, new Date(), false, true);
    }

    private void updateUi() {
        mTagContentsTextView.setText(mCardContents);
        mWriteToTagEditText.setText(editTextContents);
        mShouldWriteCheckBox.setChecked(false);
    }

    void postScanResult(ScanResult result) throws IOException {
        for (String server : mServers) {
            Request request = new Request.Builder()
                    .url(server + "/results")
                    .post(RequestBody.create(JSON_MEDIA_TYPE, result.toString()))
                    .build();

            mHttpClient.newCall(request)
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d("NFCTAG", "exception : ");
                            e.printStackTrace();
                            Log.d("NFCTAG", "Error posting scan result to " + call.request().url().host() + ", connectivity issue");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.code() == HttpURLConnection.HTTP_OK) {
                                Log.v("NFCTAG", "Successfully posted scan result to " + call.request().url().host() + "");
                            } else {
                                Log.v("NFCTAG", "Error posting scan result to " + call.request().url().host());
                            }
                            Log.v("NFCTAG", response.code() + ": " + response.body().string());
                        }
                    });
        }
    }
}
