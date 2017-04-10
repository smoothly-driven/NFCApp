package com.example.robot_server.nfcapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.example.robot_server.nfcapp.domain.StringWrapper;
import com.example.robot_server.nfcapp.domain.TestProfile;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import static com.example.robot_server.nfcapp.domain.NfcTestManager.PLAIN_TEXT_MEDIA_TYPE;

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

    @AfterViews
    protected void init() {
        if (!checkNfc()) return;
        setupLayoutComponents();
        mNfcTestManager = new NfcTestManager(this, mCardContent, mToWrite);
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

        // commented out code was replaced by AndroidAnnotataions' black magic.
        /*
        mProfileNameTextView = (TextView) findViewById(R.id.tv_profile_name);
        mWriteToTagEditText = (EditText) findViewById(R.id.et_to_write);
        mShouldWriteCheckBox = (CheckBox) findViewById(R.id.chk_write);
        mShouldReadCheckBox = (CheckBox) findViewById(R.id.chk_read);
        mStartButton = (Button) findViewById(R.id.btn_start);
        mStopButton = (Button) findViewById(R.id.btn_stop);
        mLoadProfileButton = (Button) findViewById(R.id.btn_load_profile);
        mSaveProfileButton = (Button) findViewById(R.id.btn_save_profile);
        mStatusTextView = (TextView) findViewById(R.id.tv_test_status);
        mScansTextView = (TextView) findViewById(R.id.tv_scans);
        */
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

    private void updateUi() {
        mWriteToTagEditText.setText(mToWrite.get());
        mScansTextView.setText(String.valueOf(mNfcTestManager.getScans()));
    }

    @Override
    public void updateUi(TestProfile profile) {
        triggeredInternally = true;
        mProfileNameTextView.setText(profile.getName());
        //mTestNameTextView.setText(profile.getTestName());
        mWriteToTagEditText.setText(profile.getWriteContent());
        mShouldReadCheckBox.setChecked(profile.has("read"));
        mShouldWriteCheckBox.setChecked(profile.has("write"));
        //profile.getServers();
        triggeredInternally = false;
        mScansTextView.setText(String.valueOf(mNfcTestManager.getScans()));
    }


    @Click(R.id.btn_start)
    void onStartButtonClicked() {
        mNfcTestManager.startClicked();
    }

    @Click(R.id.btn_stop)
    void onStopButtonClicked() {
        mNfcTestManager.stopClicked();
    }

    @Click(R.id.btn_load_profile)
    void onLoadButtonClicked() {
        mNfcTestManager.loadClicked();
    }

    @Click(R.id.btn_save_profile)
    void onSaveButtonClicked() {
        mNfcTestManager.saveClicked();
    }

    @Click({R.id.chk_write, R.id.chk_read})
    void onWriteCheckedChanged(CheckBox cb) {
        if (!triggeredInternally) {
            switch (cb.getId()) {
                case R.id.chk_read:
                    mNfcTestManager.readChecked(cb.isChecked());
                    break;
                case R.id.chk_write:
                    mNfcTestManager.writeChecked(cb.isChecked());
            }
        }
    }

    @AfterTextChange(R.id.et_to_write)
    void onToWriteTextChanged(Editable e) {
        if (!triggeredInternally) mToWrite.set(e.toString());
    }
}
