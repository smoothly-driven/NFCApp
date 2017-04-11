package com.example.robot_server.nfcapp.domain;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.Serializable;

/*package*/ class Server implements Serializable {

    @SerializedName("ip")
    private String mIp;
    @SerializedName("alias")
    private String mAlias;

    /*package*/ Server(String ip, String alias) {
        this.mIp = ip;
        this.mAlias = alias;
    }

    String getIp() {
        return mIp;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    /*package*/ JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("ip", mIp);
            json.put("alias", mAlias);
            return json;
        } catch (org.json.JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
