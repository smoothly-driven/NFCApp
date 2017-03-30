package com.example.robot_server.nfcapp.domain;

import java.io.Serializable;

public class StringWrapper implements Serializable {

    private String mString;

    public StringWrapper() {
        mString = "";
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
    public int hashCode() {
        return mString.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || (getClass() != o.getClass() && o.getClass() != String.class)) return false;

        if (o.getClass() == getClass()) {
            StringWrapper that = (StringWrapper) o;
            return that.get().equals(mString);
        }

        return mString.equals(o);
    }
}
