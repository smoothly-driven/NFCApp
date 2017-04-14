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
import com.example.robot_server.nfcapp.utils.JsonUtils;
import com.example.robot_server.nfcapp.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    private static final String KEY_PROPERTIES = "properties";
    private static final String KEY_TO_WRITE = "toWrite";
    private static final String PROPERTY_READ = "read";
    private static final String PROPERTY_WRITE = "write";
    private static final String DEFAULT_SERVER_IP = "http://10.111.17.139:5000/api/nfc";
    //private static final String PROFILES_ENDPOINT = "/profiles";
    private static final String PROFILE_ENDPOINT = "/profile";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    private static final String RESULTS_ENDPOINT = "/results";
    private final Gson mGson;

    private NfcTestController mController;
    private Handler mHandler;
    private TestProfile mProfile;
    private JSONObject mSourceProfile;
    private int mScans;
    private String mImei;
    private String mFamocoId;
    private String mModel;
    private String mImage;

    private boolean isRunning, isPaused;

    @SuppressWarnings("all")
    public NfcTestManager(Context context) {
        if (!(context instanceof NfcTestController)) {
            throw new IllegalArgumentException("Context is not an instance of NfcTestController");
        }

        mController = (NfcTestController) context;
        mHandler = new Handler(Looper.getMainLooper());

        mSourceProfile = new JSONObject();

        mGson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .create();

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
            ScanResult.ScanResultBuilder builder = new ScanResult.ScanResultBuilder()
                    .imei(mImei)
                    .image(mImage)
                    .model(mModel)
                    .famocoId(mFamocoId)
                    .testProfile(mProfile.getName())
                    .scans(++mScans);

            for (IntentProcessor processor : mProfile) {
                processor.process(intent, builder);
            }
            long scanDuration = SystemClock.elapsedRealtime() - intentReceptionTime;
            builder.scanDuration(scanDuration)
                    .timestamp(new Date());

            Toast.makeText((Context) mController, mProfile.getName() + " took " + scanDuration + " ms.", Toast.LENGTH_SHORT).show();

            ScanResult scanResult = builder.build();
            postScanResult(scanResult);

        }
    }

    private void postScanResult(ScanResult result) {
        postResult(RESULTS_ENDPOINT, mGson.toJson(result, ScanResult.class));
    }

    private void loadProfile(JSONObject profile) {
        this.mSourceProfile = profile;
        checkServers();
        mController.updateUi();
    }

    public boolean has(String key) {
        return has(key, true);
    }

    private boolean has(String key, boolean isProperty) {
        if (isProperty) {
            JSONArray properties = mSourceProfile.optJSONArray(KEY_PROPERTIES);
            return properties != null && JsonUtils.arrayContains(properties, key);
        }
        return mSourceProfile.has(key);
    }

    public Object get(String key) {
        Object obj = mSourceProfile.opt(key);
        if (obj == null) {
            return "";
        }
        return obj;
    }

    private void syncTestProfile() {
        Request request = new Request.Builder()
                .url(DEFAULT_SERVER_IP + PROFILE_ENDPOINT)
                .get()
                .build();
        HttpUtils.send(request, getProfileSyncCallback());
    }

    private void postResult(String endpoint, String body) {
        for (Server server : mProfile.getServers()) {
            Request request = new Request.Builder()
                    .url(server.getIp() + endpoint)
                    .post(RequestBody.create(JSON_MEDIA_TYPE, body))
                    .build();
            HttpUtils.send(request);
        }
    }

    private void checkServers() {
        try {
            JSONArray servers = mSourceProfile.optJSONArray("servers");
            if (servers == null) {
                servers = new JSONArray();
                mSourceProfile.put("servers", servers);
            }
            if (servers.length() == 0) {
                JSONObject defaultServer = new JSONObject();
                defaultServer.put("alias", "Default server");
                defaultServer.put("ip", DEFAULT_SERVER_IP);
                servers.put(defaultServer);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void startTest() {
        mScans = 0;
        mProfile = mGson.fromJson(mSourceProfile.toString(), TestProfile.class).setup(mSourceProfile);
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
    public void onStartTest() {
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
    public void onStopTest() {
        stopTest();
    }

    /*
     * Called when the state of the read checkbox changes.
     */
    public void onReadChanged(boolean read) {
        changeProperty(IntentProcessor.READ, read);
    }

    /*
     * Called when the state of the write checkbox changes.
     */
    public void onWriteChanged(boolean write) {
        changeProperty(IntentProcessor.WRITE, write);
    }

    private void changeProperty(String propertyValue, boolean property) {
        if (property) {
            mSourceProfile.optJSONArray(KEY_PROPERTIES).put(propertyValue);
        } else {
            JsonUtils.removeFromArray(mSourceProfile.optJSONArray(KEY_PROPERTIES), propertyValue);
        }
    }

    /*
     * Called when the toWrite content changes.
     */
    public void onToWriteChanged(String toWrite) {
        try {
            mSourceProfile.put(KEY_TO_WRITE, toWrite);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * When the save profile button is clicked
     */
    public void onSave() {
        if (mSourceProfile != null) {
            postResult(PROFILE_ENDPOINT, mSourceProfile.toString());
        }
    }

    /*
     * When the load profile button is clicked
     */
    public void onLoad() {
        syncTestProfile();
    }

    private Callback getProfileSyncCallback() {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("NFCTAG", "Http request failed : \n" + call.toString());
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
    }
}
