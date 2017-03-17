package com.example.robot_server.nfcapp;

import java.io.Serializable;

/**
 * Created by robot-server on 13.03.17.
 */

public class StringWrapper implements Serializable {

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

    @Override
    public boolean equals(Object obj) {
        return mString.equals(obj);
    }

}
