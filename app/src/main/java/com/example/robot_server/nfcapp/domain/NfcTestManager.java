package com.example.robot_server.nfcapp.domain;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.example.robot_server.nfcapp.NfcTestController;
import com.example.robot_server.nfcapp.processors.IntentProcessor;
import com.example.robot_server.nfcapp.utils.HttpUtils;
import com.example.robot_server.nfcapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NfcTestManager {

    public static final String PLAIN_TEXT_MEDIA_TYPE = "text/plain";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    private static final String DEFAULT_SERVER_IP = "http://10.111.17.139:5000/api/nfc";
    private static final String RESULTS_ENDPOINT = "/results";
    //private static final String PROFILES_ENDPOINT = "/profiles";
    private static final String PROFILE_ENDPOINT = "/profile";

    private NfcTestController mController;
    private Handler mHandler;
    private TestProfile mProfile;

    private JSONObject mSourceProfile;

    private int mScans;
    private String mImei;
    private String mFamocoId;
    private String mModel;
    private String mImage;

    private StringWrapper mCardContent;
    private StringWrapper mToWrite;

    private boolean isRunning, isPaused;

    @SuppressWarnings("all")
    public NfcTestManager(Context context, StringWrapper cardContent, StringWrapper toWrite) {
        if (!(context instanceof NfcTestController)) {
            throw new IllegalArgumentException("Context is not an instance of NfcTestController");
        }
        mCardContent = cardContent;
        mToWrite = toWrite;

        mSourceProfile = new JSONObject();

        mController = (NfcTestController) context;
        mHandler = new Handler(Looper.getMainLooper());
        mProfile = new TestProfile();

        this.mImei = Utils.getDeviceImei(context);
        this.mFamocoId = Build.SERIAL;
        this.mImage = Build.DISPLAY;
        this.mModel = Build.MODEL;
    }

    public int getScans() {
        return mScans;
    }

    public void handleIntent(Intent intent) {
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

            Toast.makeText((Context) mController, mProfile.getName() + " took " + scanDuration + " ms.", Toast.LENGTH_SHORT).show();

            ScanResult scanResult = builder.build();
            postScanResult(scanResult);
        }
    }

    private void postScanResult(ScanResult result) {
        postJson(RESULTS_ENDPOINT, result.toJson());
    }

    private void postJson(String endpoint, JSONObject body) {
        for (Server server : mProfile.getServers()) {
            Request request = new Request.Builder()
                    .url(server.getIp() + endpoint)
                    .post(RequestBody.create(JSON_MEDIA_TYPE, body.toString()))
                    .build();
            HttpUtils.send(request);
        }
    }

    private void loadProfile(JSONObject profile) {
        this.mSourceProfile = profile;
    }

    public boolean has(String key) {
        return has(key, true);
    }

    public boolean has(String key, boolean isProperty) {
        if (isProperty) {
            JSONArray properties = mSourceProfile.optJSONArray("properties");
            if (properties != null) {
                for (int i = 0; i < properties.length(); i++) {
                    if (key.equals(properties.opt(i))) return true;
                }
            }
        }
        return mSourceProfile.has(key);
    }

    public void set(String key) {
        set(key, null, true);
    }

    public void set(String key, Object value, boolean isProperty) {
        try {
            if (isProperty) {
                if (mSourceProfile.opt("properties") == null) mSourceProfile.put("properties", new JSONArray());
                JSONArray properties = mSourceProfile.optJSONArray("properties");
                properties.put(key);
            } else {
                mSourceProfile.put(key, value);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void syncTestProfile() {
        Request request = new Request.Builder()
                .url(DEFAULT_SERVER_IP + PROFILE_ENDPOINT)
                .get()
                .build();
        Callback c = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("NFCTAG", "Call failed");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            loadProfile(new JSONObject(response.body().string()));
                        } catch (java.io.IOException | org.json.JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        };
        HttpUtils.send(request, c);
    }

    private void startTest() {
        mScans = 0;
        mProfile.setup();
        mController.startTest();
        isRunning = true;
        isPaused = false;
    }

    private void pauseTest() {
        isPaused = true;
        mController.pauseTest();
    }

    private void resumeTest() {
        isRunning = true;
        isPaused = false;
        mController.resumeTest();
    }

    private void stopTest() {
        isPaused = false;
        isRunning = false;
        mController.stopTest();
    }

    /*
     * Called when the start/pause/resume test button is clicked.
     */
    public void startClicked() {
        if (isPaused) {
            resumeTest();
        } else if (isRunning) {
            pauseTest();
        } else {
            startTest();
        }
    }

    /*
     * Called when the stop test button is clicked.
     */
    public void stopClicked() {
        stopTest();
    }

    /*
     * Called when the state of the read checkbox changes.
     */
    public void readChecked(boolean read) {
        mProfile.readContent(mCardContent);
        mProfile.read(read);
    }

    /*
     * Called when the state of the write checkbox changes.
     */
    public void writeChecked(boolean write) {
        mProfile.toWrite(mToWrite);
        mProfile.write(write);
    }

    /*
     * When the save profile button is clicked
     */
    public void saveClicked() {
        JSONObject profile = mProfile.toJson();
        if (profile != null) {
            postJson(PROFILE_ENDPOINT, profile);
        }
    }

    /*
     * When the load profile button is clicked
     */
    public void loadClicked() {
        syncTestProfile();
    }
}
