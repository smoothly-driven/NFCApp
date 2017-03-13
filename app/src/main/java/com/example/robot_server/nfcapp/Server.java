package com.example.robot_server.nfcapp;

/**
 * Created by robot-server on 13.03.17.
 */

public class Server {

    private String mIp;
    private String mAlias;

    public Server(String ip, String alias) {
        this.mIp = ip;
        this.mAlias = alias;
    }

    public String getIp() {
        return mIp;
    }

    public String getAlias() {
        return mAlias;
    }

    @Override
    public String toString() {
        return mAlias + '@' + mIp;
    }

    public static Server fromString(String server) {
        String[] attributes = server.split("@");
        return new Server(attributes[1], attributes[0]);
    }
}
