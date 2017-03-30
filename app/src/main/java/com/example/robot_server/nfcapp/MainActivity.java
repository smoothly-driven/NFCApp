package com.example.robot_server.nfcapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.robot_server.nfcapp.domain.NfcTestManager;
import com.example.robot_server.nfcapp.domain.StringWrapper;
import com.example.robot_server.nfcapp.profiles.TestProfile;

import static com.example.robot_server.nfcapp.domain.NfcTestManager.PLAIN_TEXT_MEDIA_TYPE;

public class MainActivity extends AppCompatActivity implements NfcTestController {

    public static final String START_TEST_TEXT = "Start test";
    public static final String STOP_TEST_TEXT = "Stop test";
    public static final String PAUSE_TEST_TEXT = "Pause test";
    public static final String RESUME_TEST_TEXT = "Resume test";

    public static final String STATUS_RUNNING = "Running";
    public static final String STATUS_PAUSED = "Paused";
    public static final String STATUS_NOT_RUNNING = "Not running";

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
    private StringWrapper mToWrite;

    private NfcTestManager mNfcTestManager;
    private boolean triggeredInternally = false; //set to true when programmatically changing the value of checkboxes and such. Allows to control when to ignore listener calls

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
        mNfcTestManager = new NfcTestManager(this, mCardContent, mToWrite);
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /*
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
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
        mToWrite = new StringWrapper("");
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
        mScansTextView.setText(String.valueOf(0));
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mNfcTestManager.handleIntent(intent);
        updateUi();
    }

    @Override
    public void startTest() {
        mWriteToTagEditText.setEnabled(false);
        mShouldWriteCheckBox.setEnabled(false);
        mShouldReadCheckBox.setEnabled(false);
        mStopButton.setEnabled(true);
        mStartButton.setText(PAUSE_TEST_TEXT);
        mStatusTextView.setText(STATUS_RUNNING);
        mStatusTextView.setTextColor(Color.GREEN);
        updateUi();
    }

    @Override
    public void pauseTest() {
        mStartButton.setText(RESUME_TEST_TEXT);
        mStatusTextView.setText(STATUS_PAUSED);
        mStatusTextView.setTextColor(Color.BLUE);
    }

    @Override
    public void resumeTest() {
        mStartButton.setText(PAUSE_TEST_TEXT);
        mStatusTextView.setText(STATUS_RUNNING);
        mStatusTextView.setTextColor(Color.GREEN);
        updateUi();
    }

    @Override
    public void stopTest() {
        mWriteToTagEditText.setEnabled(true);
        mShouldWriteCheckBox.setEnabled(true);
        mShouldReadCheckBox.setEnabled(true);
        mStopButton.setEnabled(false);
        mStartButton.setText(START_TEST_TEXT);
        mStatusTextView.setText(STATUS_NOT_RUNNING);
        mStatusTextView.setTextColor(Color.RED);
    }

    @Override
    public void updateUi() {
        mTagContentsTextView.setText(mCardContent.get());
        mWriteToTagEditText.setText(mToWrite.get());
        mScansTextView.setText(String.valueOf(mNfcTestManager.getScans()));
    }

    public void updateUi(TestProfile profile) {
        triggeredInternally = true;
        mTagContentsTextView.setText(profile.getReadContent());
        mWriteToTagEditText.setText(profile.getWriteContent());
        mShouldReadCheckBox.setChecked(profile.getRead());
        mShouldWriteCheckBox.setChecked(profile.getWrite());
        triggeredInternally = false;
        mScansTextView.setText(String.valueOf(mNfcTestManager.getScans()));
    }

    private class OnButtonClickedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_start:
                    mNfcTestManager.startClicked();
                    break;
                case R.id.btn_stop:
                    mNfcTestManager.stopClicked();
                    break;
                case R.id.btn_load_profile:
                    mNfcTestManager.loadClicked();
                    break;
                case R.id.btn_save_profile:
                    mNfcTestManager.saveClicked();
                    break;
            }
        }
    }

    private class OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!triggeredInternally) {
                switch (buttonView.getId()) {
                    case R.id.chk_read:
                        mNfcTestManager.readChecked(isChecked);
                        break;
                    case R.id.chk_write:
                        mNfcTestManager.writeChecked(isChecked);
                        break;
                }
            }
        }
    }

    private class OnTextChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            if (!triggeredInternally) mToWrite.set(s.toString());
        }
    }
}
