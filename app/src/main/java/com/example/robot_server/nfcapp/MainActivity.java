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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import processors.IntentProcessor;
import processors.MetaProcessor;
import processors.ReadProcessor;
import processors.WriteProcessor;

public class MainActivity extends AppCompatActivity {

    public static final String PLAIN_TEXT_MEDIA_TYPE = "text/plain";
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    public static final String DEFAULT_SERVER_IP = "http://10.111.17.139:5000";
    public static final String KEY_SERVERS = "servers";
    public static final String RESULTS_ENDPOINT = "/results";

    private Set<Server> mServers;
    private List<IntentProcessor> mProcessors;
    private OkHttpClient mHttpClient;
    private NfcAdapter mNfcAdapter;
    private TextView mTagContentsTextView;
    private EditText mWriteToTagEditText;
    private CheckBox mShouldWriteCheckBox;
    private CheckBox mShouldReadCheckBox;

    private StringWrapper mCardContents = new StringWrapper("");
    private StringWrapper editTextContents = new StringWrapper("");

    private int mScans;

    private String mIMEI;
    private String mFamocoId;
    private String mModel;
    private String mImage;

    private boolean isRunning, isPaused;

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
        filters[1] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
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

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Log.e("NFCTAG", "The device doesn't have NFC");
            finish();
            return;
        }

        mTagContentsTextView = (TextView) findViewById(R.id.text_view);
        mWriteToTagEditText = (EditText) findViewById(R.id.edit_text);
        mShouldWriteCheckBox = (CheckBox) findViewById(R.id.chk_write);
        mShouldReadCheckBox = (CheckBox) findViewById(R.id.chk_read);

        mShouldReadCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mProcessors.add(1, new ReadProcessor(mCardContents));
                } else {
                    removeProcessor("ReadProcessor");
                }
            }
        });

        mShouldWriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mProcessors.add(new WriteProcessor(editTextContents));
                } else {
                    removeProcessor("WriteProcessor");
                }
            }
        });

        mWriteToTagEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                editTextContents.set(s.toString());
            }
        });

        mHttpClient = new OkHttpClient();

        mServers = PreferenceUtils.stringsToServerSet(PreferenceUtils.getStringSet(this, KEY_SERVERS));
        if (mServers == null) {
            mServers = new HashSet<>();
            mServers.add(new Server(DEFAULT_SERVER_IP, "Default server"));
        }

        mProcessors = new ArrayList<>();
        mProcessors.add(new MetaProcessor());
        //mProcessors.add(new ReadProcessor(mCardContents));
        //mProcessors.add(new WriteProcessor(editTextContents));

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        this.mIMEI = tm.getDeviceId();
        this.mFamocoId = Build.SERIAL;
        this.mImage = Build.DISPLAY;
        this.mModel = Build.MODEL;
    }

    private void addServer(String ip, String alias) {
        mServers.add(new Server(ip, alias));
        PreferenceUtils.putStringSet(this, KEY_SERVERS, PreferenceUtils.serversToStringSet(mServers));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        long intentReceptionTime = SystemClock.elapsedRealtime(); //apparently better than System.currentTimeMillis() ?
        mScans++;
        ScanResult.ScanResultBuilder builder = new ScanResult.ScanResultBuilder(mIMEI, mFamocoId, mModel, mImage).scans(mScans);
        for (IntentProcessor processor : mProcessors) {
            processor.process(intent, builder);
        }
        //handleIntent(intent, builder);
        long scanDuration = SystemClock.elapsedRealtime() - intentReceptionTime;
        builder.scanDuration(scanDuration)
                .timestamp(new Date())
                .detectOnly(false)
                .readContent(true);
        ScanResult scanResult = builder.build();
        try {
            postScanResult(scanResult);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        updateUi();
        Toast.makeText(this, mShouldWriteCheckBox.isChecked() ? "Reading + writing took " : "Reading took " + scanDuration + " ms.", Toast.LENGTH_SHORT).show();

    }

    private void removeProcessor(String simpleClassName) {
        for (int i = 0; i < mProcessors.size(); i++) {
            if (mProcessors.get(i).getClass().getSimpleName().equals(simpleClassName)) {
                mProcessors.remove(i);
                break;
            }
        }
    }

    private void handleIntent(Intent intent, ScanResult.ScanResultBuilder builder) {
        long intentReceptionTime = SystemClock.elapsedRealtime(); //apparently better than System.currentTimeMillis() ?
        Log.v("NFCTAG", intent.getAction());
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        builder.cardTechnology(detectedTag.getTechList())
                .cardUid(NFCUtils.byteArrayToHex(detectedTag.getId()));
        if (mShouldWriteCheckBox.isChecked()) {
            int opStatus = NFCUtils.writeTag(NFCUtils.getMessageAsNdef(mWriteToTagEditText.getText().toString()), detectedTag);
            Log.v("NFCTAG", "writing operation returned a code " + opStatus);
            if (opStatus == NFCUtils.CODE_SUCCESS) {
                mCardContents.set(mWriteToTagEditText.getText().toString());
                builder.cardContent(mCardContents.get());
                editTextContents.set("");
            }
        } else {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                }
                mCardContents.set(NFCUtils.readMessageContents(messages));
                builder.cardContent(mCardContents.get());
                Log.v("NFCTAG", "messages were successfully decoded : " + mCardContents);
                editTextContents.set(mWriteToTagEditText.getText().toString());
            }
        }
        mScans++;
        long scanDuration = SystemClock.elapsedRealtime() - intentReceptionTime;
        builder.scans(mScans)
                .scanDuration(scanDuration)
                .timestamp(new Date())
                .detectOnly(false)
                .readContent(true);

        Toast.makeText(this, mShouldWriteCheckBox.isChecked() ? "Reading + writing took " : "Reading took " + scanDuration + " ms.", Toast.LENGTH_SHORT).show();
    }

    private void updateUi() {
        mTagContentsTextView.setText(mCardContents.get());
        mWriteToTagEditText.setText(editTextContents.get());
        mShouldWriteCheckBox.setChecked(false);
    }

    void postScanResult(ScanResult result) throws IOException {
        for (Server server : mServers) {
            Request request = new Request.Builder()
                    .url(server.getIp() + RESULTS_ENDPOINT)
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
