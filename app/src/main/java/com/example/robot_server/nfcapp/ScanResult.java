package com.example.robot_server.nfcapp;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by robot-server on 06.03.17.
 */

public class ScanResult {

    private String mCardContent;
    private String[] mCardTechnology;
    private long mScanDuration;
    private int mScans;

    private String mImei;
    private String mFamocoId;
    private String mModel;
    private String mImage;

    private boolean detectOnly = false;
    private boolean readContent = true;

    private Date mTimestamp;

    public ScanResult(String tagContents, String[] cardTechnology, int scans, String imei, String famocoId, String model, String image, long scanDuration, Date timestamp, boolean detectOnly, boolean readContent) {
        this.mCardContent = tagContents;
        this.mCardTechnology = cardTechnology;
        this.mScans = scans;
        this.mImei = imei;
        this.mFamocoId = famocoId;
        this.mModel = model;
        this.mImage = image;
        this.mScanDuration = scanDuration;
        this.mTimestamp = timestamp;
        this.detectOnly = detectOnly;
        this.readContent = readContent;
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
            jsonBody.put("card_technology", mCardTechnology);
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
