package com.example.robot_server.nfcapp.domain;

import com.example.robot_server.nfcapp.annotations.Inject;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScanResult {

    @SerializedName("cardUuid")
    private final String mCardUid;
    @SerializedName("cardContent")
    private final String mCardContent;
    @SerializedName("cardTechnology")
    private final String[] mCardTechnology;
    @SerializedName("identifiers")
    private final Map<String, String> mIdentifiers;
    @SerializedName("scanDuration")
    private final long mScanDuration;
    @SerializedName("scans")
    private final int mScans;
    @SerializedName("@timestamp")
    private final Date mTimestamp;
    @SerializedName("testProfileName")
    private String mTestProfile;

    //TODO : dependency injector
    @Inject()
    private transient Gson mGson;

    private ScanResult(ScanResultBuilder builder) {
        this.mCardUid = builder.mCardUid;
        this.mCardContent = builder.mCardContent;
        this.mCardTechnology = builder.mCardTechnology;
        this.mScans = builder.mScans;
        this.mScanDuration = builder.mScanDuration;
        this.mTimestamp = builder.mTimestamp;
        this.mTestProfile = builder.mTestProfile;
        this.mIdentifiers = builder.mIdentifiers;
    }

    JSONObject toJson() {
        JSONObject identifiers = new JSONObject();
        JSONObject testDetails = new JSONObject();
        JSONObject jsonBody = new JSONObject();
        try {
            for (Map.Entry entry : mIdentifiers.entrySet()) {
                identifiers.put((String) entry.getKey(), entry.getValue());
            }
            testDetails.put("testProfile", mTestProfile);
            jsonBody.put("card_uid", mCardUid);
            jsonBody.put("card_technology", new JSONArray(mCardTechnology));
            jsonBody.put("card_contents", mCardContent);
            jsonBody.put("scan_duration", mScanDuration);
            jsonBody.put("scans", mScans);
            jsonBody.put("@timestamp", mTimestamp);
            jsonBody.put("identifiers", identifiers);
            jsonBody.put("testDetails", testDetails);
        } catch (org.json.JSONException ex) {
            ex.printStackTrace();
        }
        return jsonBody;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public static class ScanResultBuilder {

        private String mCardUid;
        private String mCardContent;
        private Map<String, String> mIdentifiers;
        private String[] mCardTechnology;
        private long mScanDuration;
        private int mScans;

        private Date mTimestamp;
        private String mTestProfile;

        /*package*/ ScanResultBuilder() {
            mIdentifiers = new HashMap<>();
        }

        public ScanResultBuilder imei(String imei) {
            mIdentifiers.put("imei", imei);
            return this;
        }

        public ScanResultBuilder image(String image) {
            mIdentifiers.put("image", image);
            return this;
        }

        public ScanResultBuilder famocoId(String famocoId) {
            mIdentifiers.put("famocoId", famocoId);
            return this;
        }

        public ScanResultBuilder model(String model) {
            mIdentifiers.put("model", model);
            return this;
        }

        public ScanResultBuilder cardUid(String cardUid) {
            mCardUid = cardUid;
            return this;
        }

        public ScanResultBuilder cardContent(String cardContent) {
            mCardContent = cardContent;
            return this;
        }

        public ScanResultBuilder cardTechnology(String[] cardTechnology) {
            mCardTechnology = cardTechnology;
            return this;
        }

        ScanResultBuilder scanDuration(long scanDuration) {
            mScanDuration = scanDuration;
            return this;
        }

        ScanResultBuilder scans(int scans) {
            mScans = scans;
            return this;
        }

        ScanResultBuilder timestamp(Date timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        ScanResultBuilder testProfile(String testProfile) {
            mTestProfile = testProfile;
            return this;
        }

        ScanResult build() {
            return new ScanResult(this);
        }
    }
}
