package com.example.robot_server.nfcapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by robot-server on 06.03.17.
 */
public class ScanResult1Builder {

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

    private boolean detectOnly;
    private boolean readContent;

    public ScanResult1Builder() {

    }

    public ScanResult1Builder(String imei, String famocoId, String model, String image) {
        this.mImei = imei;
        this.mFamocoId = famocoId;
        this.mModel = model;
        this.mImage = image;
    }

    public ScanResult1Builder cardUid(String cardUid) {
        mCardUid = cardUid;
        return this;
    }

    public ScanResult1Builder cardContent(String cardContent) {
        mCardContent = cardContent;
        return this;
    }

    public ScanResult1Builder cardTechnology(String[] cardTechnology) {
        mCardTechnology = cardTechnology;
        return this;
    }

    public ScanResult1Builder scanDuration(long scanDuration) {
        mScanDuration = scanDuration;
        return this;
    }

    public ScanResult1Builder scans(int scans) {
        mScans = scans;
        return this;
    }

    public ScanResult1Builder imei(String imei) {
        mImei = imei;
        return this;
    }

    public ScanResult1Builder famocoId(String famocoId) {
        mFamocoId = famocoId;
        return this;
    }

    public ScanResult1Builder model(String model) {
        mModel = model;
        return this;
    }

    public ScanResult1Builder image(String image) {
        mImage = image;
        return this;
    }

    public ScanResult1Builder timestamp(Date timestamp) {
        mTimestamp = timestamp;
        return this;
    }

    public ScanResult1Builder detectOnly(boolean detectOnly) {
        this.detectOnly = detectOnly;
        return this;
    }

    public ScanResult1Builder readContent(boolean readContent) {
        this.readContent = readContent;
        return this;
    }

    public ScanResult1 build() {
        return new ScanResult1(this);
    }

    public class ScanResult1 {

        private final String mCardUid;
        private final String mCardContent;
        private final String[] mCardTechnology;
        private final long mScanDuration;
        private final int mScans;

        private final String mImei;
        private final String mFamocoId;
        private final String mModel;
        private final String mImage;

        private final boolean detectOnly;
        private final boolean readContent;

        private final Date mTimestamp;

        private ScanResult1(ScanResult1Builder builder) {
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
            this.detectOnly = builder.detectOnly;
            this.readContent = builder.readContent;
        }

        public JSONObject toJson() {
            JSONObject identifiers = new JSONObject();
            JSONObject testDetails = new JSONObject();
            JSONObject jsonBody = new JSONObject();
            try {
                identifiers.put("image", mImage);
                identifiers.put("famoco_id", mFamocoId);
                identifiers.put("model", mModel);
                identifiers.put("imei", mImei);
                testDetails.put("readContent", readContent);
                testDetails.put("detectOnly", detectOnly);
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
    }
}
