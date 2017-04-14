package com.example.robot_server.nfcapp;

import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.robot_server.nfcapp.domain.NfcTestManager;
import com.example.robot_server.nfcapp.utils.Utils;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements NfcTestController {

    public static final String START_TEST_TEXT = "Start test";
    public static final String STOP_TEST_TEXT = "Stop test";
    public static final String PAUSE_TEST_TEXT = "Pause test";
    public static final String RESUME_TEST_TEXT = "Resume test";

    public static final String STATUS_RUNNING = "Running";
    public static final String STATUS_PAUSED = "Paused";
    public static final String STATUS_NOT_RUNNING = "Not running";

    @ViewById(R.id.tv_profile_name)
    /*package*/ TextView mProfileNameTextView;
    @ViewById(R.id.tv_test_name)
    /*package*/ TextView mTestNameTextView;
    @ViewById(R.id.et_to_write)
    /*package*/ EditText mWriteToTagEditText;
    @ViewById(R.id.chk_write)
    /*package*/ CheckBox mShouldWriteCheckBox;
    @ViewById(R.id.chk_read)
    /*package*/ CheckBox mShouldReadCheckBox;
    @ViewById(R.id.btn_start)
    /*package*/ Button mStartButton;
    @ViewById(R.id.btn_stop)
    /*package*/ Button mStopButton;
    @ViewById(R.id.btn_save_profile)
    /*package*/ Button mSaveProfileButton;
    @ViewById(R.id.btn_load_profile)
    /*package*/ Button mLoadProfileButton;
    @ViewById(R.id.tv_test_status)
    /*package*/ TextView mStatusTextView;
    @ViewById(R.id.tv_scans)
    /*package*/ TextView mScansTextView;

    private NfcAdapter mNfcAdapter;
    private NfcTestManager mNfcTestManager;
    //set to true when programmatically changing the value of checkboxes and such.
    // Allows to control when to ignore listener calls.
    private boolean triggeredInternally = false;

    @AfterViews
    /*
     * This code is invoked by AndroidAnnotations as soon as it's done binding the requested views.
     * It is essentially an onCreate.
     */
    protected void init() {
        if (!checkNfc()) return;
        AppContext.loadContext(this);
        setupLayoutComponents();
        mNfcTestManager = new NfcTestManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        //Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
        Utils.stopForegroundDispatch(this, mNfcAdapter);
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
        mStartButton.setText(START_TEST_TEXT);
        mStopButton.setText(STOP_TEST_TEXT);
        mScansTextView.setText(String.valueOf(0));
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
        mLoadProfileButton.setEnabled(false);
        mSaveProfileButton.setEnabled(false);
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
        updateUi();
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
        mLoadProfileButton.setEnabled(true);
        mSaveProfileButton.setEnabled(true);
        mStopButton.setEnabled(false);
        mStartButton.setText(START_TEST_TEXT);
        mStatusTextView.setText(STATUS_NOT_RUNNING);
        mStatusTextView.setTextColor(Color.RED);
        updateUi();
    }

    @Override
    public void updateUi() {
        triggeredInternally = true;
        mProfileNameTextView.setText((String) mNfcTestManager.get("profileName"));
        mTestNameTextView.setText((String) mNfcTestManager.get("testName"));
        mWriteToTagEditText.setText((String) mNfcTestManager.get("toWrite"));
        mShouldReadCheckBox.setChecked(mNfcTestManager.has("read"));
        mShouldWriteCheckBox.setChecked(mNfcTestManager.has("write"));
        //profile.getServers();
        triggeredInternally = false;
        mScansTextView.setText(String.valueOf(mNfcTestManager.getScans()));
    }

    @Click(R.id.btn_start)
    void onStartButtonClicked() {
        mNfcTestManager.onStartTest();
    }

    @Click(R.id.btn_stop)
    void onStopButtonClicked() {
        mNfcTestManager.onStopTest();
    }

    @Click(R.id.btn_load_profile)
    void onLoadButtonClicked() {
        mNfcTestManager.onLoad();
    }

    @Click(R.id.btn_save_profile)
    void onSaveButtonClicked() {
        mNfcTestManager.onSave();
    }

    @Click({R.id.chk_write, R.id.chk_read})
    void onWriteCheckedChanged(CheckBox cb) {
        if (!triggeredInternally) {
            switch (cb.getId()) {
                case R.id.chk_read:
                    mNfcTestManager.onReadChanged(cb.isChecked());
                    break;
                case R.id.chk_write:
                    mNfcTestManager.onWriteChanged(cb.isChecked());
            }
        }
    }

    @AfterTextChange(R.id.et_to_write)
    void onToWriteTextChanged(Editable e) {
        if (!triggeredInternally) mNfcTestManager.onToWriteChanged(e.toString());
    }
}
