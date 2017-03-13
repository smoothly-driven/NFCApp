package com.example.robot_server.nfcapp;

/**
 * Created by robot-server on 13.03.17.
 */

public class StringWrapper {

    private String mString;

    public StringWrapper() {
    }

    public StringWrapper(String string) {
        mString = string;
    }

    public void set(String string) {
        mString = string;
    }

    public String get() {
        return mString;
    }

    @Override
    public String toString() {
        return mString;
    }

}
