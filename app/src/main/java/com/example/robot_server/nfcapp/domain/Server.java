package com.example.robot_server.nfcapp.domain;

import org.json.JSONObject;

import java.io.Serializable;

public class Server implements Serializable {

    private String mIp;
    private String mAlias;

    public Server(String ip, String alias) {
        this.mIp = ip;
        this.mAlias = alias;
    }

    private static Server fromJson(JSONObject server) {
        try {
            return new Server(server.getString("ip"), server.getString("alias"));
        } catch (org.json.JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    static Server fromJsonString(String server) {
        try {
            return fromJson(new JSONObject(server));
        } catch (org.json.JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    String getIp() {
        return mIp;
    }

    /*
    public String getAlias() {
        return mAlias;
    }*/

    @Override
    public String toString() {
        return toJson().toString();
    }

    public JSONObject toJson() {
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
