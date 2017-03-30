package com.example.robot_server.nfcapp.domain;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

public class ScanResult {

    private final String mCardUid;
    private final String mCardContent;
    private final String[] mCardTechnology;
    private final long mScanDuration;
    private final int mScans;

    private final String mImei;
    private final String mFamocoId;
    private final String mModel;
    private final String mImage;
    private final Date mTimestamp;
    private String mTestProfile;

    private ScanResult(ScanResultBuilder builder) {
        this.mCardUid = builder.mCardUid;
        this.mCardContent = builder.mCardContent;
        this.mCardTechnology = builder.mCardTechnology;
        this.mScans = builder.mScans;
        this.mImei = builder.mImei;
        this.mFamocoId = builder.mFamocoId;
        this.mModel = builder.mModel;
        this.mImage = builder.mImage;
        this.mScanDuration = builder.mScanDuration;
        this.mTimestamp = builder.mTimestamp;
        this.mTestProfile = builder.mTestProfile;
    }

    JSONObject toJson() {
        JSONObject identifiers = new JSONObject();
        JSONObject testDetails = new JSONObject();
        JSONObject jsonBody = new JSONObject();
        try {
            identifiers.put("image", mImage);
            identifiers.put("famoco_id", mFamocoId);
            identifiers.put("model", mModel);
            identifiers.put("imei", mImei);
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
        private String[] mCardTechnology;
        private long mScanDuration;
        private int mScans;

        private String mImei;
        private String mFamocoId;
        private String mModel;
        private String mImage;

        private Date mTimestamp;
        private String mTestProfile;

        ScanResultBuilder(String imei, String famocoId, String model, String image) {
            this.mImei = imei;
            this.mFamocoId = famocoId;
            this.mModel = model;
            this.mImage = image;
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
