package com.example.robot_server.nfcapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robot_server.nfcapp.processors.IntentProcessor;
import com.example.robot_server.nfcapp.utils.HttpUtils;
import com.example.robot_server.nfcapp.utils.PreferenceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {

    public static final String PLAIN_TEXT_MEDIA_TYPE = "text/plain";
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    public static final String DEFAULT_SERVER_IP = "http://10.111.17.139:5000";
    public static final String KEY_SERVERS = "servers";
    public static final String RESULTS_ENDPOINT = "/results";
    public static final String PROFILES_ENDPOINT = "/profiles";

    public static final String START_TEST_TEXT = "Start test";
    public static final String STOP_TEST_TEXT = "Stop test";
    public static final String PAUSE_TEST_TEXT = "Pause test";
    public static final String RESUME_TEST_TEXT = "Resume test";

    public static final String STATUS_RUNNING = "Running";
    public static final String STATUS_PAUSED = "Paused";
    public static final String STATUS_NOT_RUNNING = "Not running";

    private Set<Server> mServers;
    private ProfileManager mProfileManager;
    private ProcessProfile mProfile;
    private NfcAdapter mNfcAdapter;
    private TextView mTagContentsTextView;
    private EditText mWriteToTagEditText;
    private CheckBox mShouldWriteCheckBox;
    private CheckBox mShouldReadCheckBox;
    private Button mStartButton;
    private Button mStopButton;
    private Button mSaveProfileButton;
    private Button mLoadProfileButton;
    private TextView mStatusTextView;
    private TextView mScansTextView;

    private StringWrapper mCardContent;
    private StringWrapper mEditTextContent;

    private int mScans;
    private String mImei;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkNfc()) return;
        setupLayoutComponents();
        setupListeners();
        loadServers();

        mProfileManager = new ProfileManager();

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        this.mImei = tm.getDeviceId();
        this.mFamocoId = Build.SERIAL;
        this.mImage = Build.DISPLAY;
        this.mModel = Build.MODEL;
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

    private void addServer(String ip, String alias) {
        mServers.add(new Server(ip, alias));
        PreferenceUtils.putStringSet(this, KEY_SERVERS, PreferenceUtils.serversToStringSet(mServers));
    }

    private boolean checkNfc() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Log.e("NFCTAG", "The device doesn't have NFC");
            finish();
            return false;
        }
        return true;
    }

    private void setupLayoutComponents() {
        mCardContent = new StringWrapper("");
        mEditTextContent = new StringWrapper("");
        mTagContentsTextView = (TextView) findViewById(R.id.tv_tag_content);
        mWriteToTagEditText = (EditText) findViewById(R.id.et_to_write);
        mShouldWriteCheckBox = (CheckBox) findViewById(R.id.chk_write);
        mShouldReadCheckBox = (CheckBox) findViewById(R.id.chk_read);
        mStartButton = (Button) findViewById(R.id.btn_start);
        mStopButton = (Button) findViewById(R.id.btn_stop);
        mLoadProfileButton = (Button) findViewById(R.id.btn_load_profile);
        mSaveProfileButton = (Button) findViewById(R.id.btn_save_profile);
        mStatusTextView = (TextView) findViewById(R.id.tv_test_status);
        mScansTextView = (TextView) findViewById(R.id.tv_scans);

        mStartButton.setText(START_TEST_TEXT);
        mStopButton.setText(STOP_TEST_TEXT);
        mScansTextView.setText(String.valueOf(mScans));
    }

    private void setupListeners() {
        CompoundButton.OnCheckedChangeListener chkListener = new OnCheckedChangeListener();
        mShouldReadCheckBox.setOnCheckedChangeListener(chkListener);
        mShouldWriteCheckBox.setOnCheckedChangeListener(chkListener);

        TextWatcher textChangeListener = new OnTextChangeListener();
        mWriteToTagEditText.addTextChangedListener(textChangeListener);
        View.OnClickListener buttonClickListener = new OnButtonClickedListener();

        mStartButton.setOnClickListener(buttonClickListener);
        mStopButton.setOnClickListener(buttonClickListener);
        mLoadProfileButton.setOnClickListener(buttonClickListener);
        mSaveProfileButton.setOnClickListener(buttonClickListener);
    }

    private void loadServers() {
        mServers = PreferenceUtils.stringsToServerSet(PreferenceUtils.getStringSet(this, KEY_SERVERS));
        if (mServers == null) {
            mServers = new HashSet<>();
            mServers.add(new Server(DEFAULT_SERVER_IP, "Default server"));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (isRunning && !isPaused) {
            long intentReceptionTime = SystemClock.elapsedRealtime(); //apparently better than System.currentTimeMillis() ?
            ScanResult.ScanResultBuilder builder = new ScanResult.ScanResultBuilder(mImei, mFamocoId, mModel, mImage).scans(++mScans);
            for (IntentProcessor processor : mProfile) {
                processor.process(intent, builder);
            }
            long scanDuration = SystemClock.elapsedRealtime() - intentReceptionTime;
            builder.scanDuration(scanDuration)
                    .timestamp(new Date())
                    .testProfile(mProfile.getName());

            Toast.makeText(this, mProfile.getName() + " took " + scanDuration + " ms.", Toast.LENGTH_SHORT).show();

            ScanResult scanResult = builder.build();
            postScanResult(scanResult);

            updateUi();
        }
    }

    private void startTest() {
        if (!isPaused) {
            mScans = 0;
            mProfile = mProfileManager.buildProfile();
            mWriteToTagEditText.setEnabled(false);
            mShouldWriteCheckBox.setEnabled(false);
            mShouldReadCheckBox.setEnabled(false);
            mStopButton.setEnabled(true);
        }
        mStartButton.setText(PAUSE_TEST_TEXT);
        mStatusTextView.setText(STATUS_RUNNING);
        mStatusTextView.setTextColor(Color.GREEN);
        isRunning = true;
        isPaused = false;
        updateUi();
    }

    private void pauseTest() {
        mStartButton.setText(RESUME_TEST_TEXT);
        mStatusTextView.setText(STATUS_PAUSED);
        mStatusTextView.setTextColor(Color.BLUE);
        isPaused = true;
    }

    private void stopTest() {
        mWriteToTagEditText.setEnabled(true);
        mShouldWriteCheckBox.setEnabled(true);
        mShouldReadCheckBox.setEnabled(true);
        mStopButton.setEnabled(false);
        mStartButton.setText(START_TEST_TEXT);
        mStatusTextView.setText(STATUS_NOT_RUNNING);
        mStatusTextView.setTextColor(Color.RED);
        isPaused = false;
        isRunning = false;
    }

    private void updateUi() {
        mTagContentsTextView.setText(mCardContent.get());
        mWriteToTagEditText.setText(mEditTextContent.get());
        mScansTextView.setText(String.valueOf(mScans));
    }

    void postScanResult(ScanResult result) {
        postJson(RESULTS_ENDPOINT, result.toJson());
    }

    void postJson(String endpoint, JSONObject body) {
        for (Server server : mServers) {
            Request request = new Request.Builder()
                    .url(server.getIp() + endpoint)
                    .post(RequestBody.create(JSON_MEDIA_TYPE, body.toString()))
                    .build();
            HttpUtils.sendPost(request);
        }
    }

    public void saveProfile(String profileName) {
        JSONObject profile = new JSONObject();
        try {
            profile.put("toWrite", mEditTextContent.get());
            profile.put("read", mShouldReadCheckBox.isChecked());
            profile.put("write", mShouldWriteCheckBox.isChecked());
            profile.put("servers", new JSONArray(PreferenceUtils.serversToStringSet(mServers)));

            postJson(PROFILES_ENDPOINT, profile);
        } catch (org.json.JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void loadProfile(JSONObject profile) {
        try {
            mEditTextContent.set(profile.getString("toWrite"));
            mShouldReadCheckBox.setChecked(profile.getBoolean("read"));
            mShouldWriteCheckBox.setChecked(profile.getBoolean("write"));
            JSONArray servers = profile.getJSONArray("servers");
            mServers.clear();
            for (int i = 0; i < servers.length(); i++) {
                mServers.add(Server.fromJsonString(servers.getString(i)));
            }
            updateUi();
        } catch (org.json.JSONException ex) {
            ex.printStackTrace();
        }
    }

    class OnButtonClickedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_start:
                    if (!isRunning || isPaused) {
                        startTest();
                    } else {
                        pauseTest();
                    }
                    break;
                case R.id.btn_stop:
                    stopTest();
                    break;
                case R.id.btn_load_profile:
                    String s = "{'_id': 'AVrc4R_aTYPWWJ5KOXtx','read': true,'servers': [{'alias':'Default server','ip':'http:\\/\\/10.111.17.139:5000'}],'toWrite': 'Bull','write': true}";
                    try {
                        loadProfile(new JSONObject(s));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.btn_save_profile:
                    saveProfile("profile1");
                    break;
            }
        }
    }

    class OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.chk_read:
                    mProfileManager.setRead(isChecked, mCardContent); // Give the processor a place to store the result
                    break;
                case R.id.chk_write:
                    mProfileManager.setWrite(isChecked, mEditTextContent); // Give the processor what to write
                    break;
            }
        }
    }

    class OnTextChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mEditTextContent.set(s.toString());
        }
    }
}
