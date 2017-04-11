package com.example.robot_server.nfcapp.domain;

import android.util.Log;

import com.example.robot_server.nfcapp.processors.IntentProcessor;
import com.example.robot_server.nfcapp.processors.ProcessorFactory;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestProfile implements Iterable<IntentProcessor> {

    private static transient Map<String, Integer> propertyMap = new HashMap<>();

    //TODO : dependency injector. Makes all of this disappear.
    static {
        propertyMap.put("read", IntentProcessor.READ);
        propertyMap.put("write", IntentProcessor.WRITE);
    }

    @SerializedName("properties")
    private List<String> mProperties;
    @SerializedName("toWrite")
    private String mToWrite; //To keep or not to keep ?
    @SerializedName("servers")
    private Set<Server> mServers;
    @SerializedName("id")
    private int mId;
    @SerializedName("profileName")
    private String mProfileName;
    private transient List<IntentProcessor> mProcessors;

    /*package*/ TestProfile() {
        mProfileName = "";
        mToWrite = "";
        mServers = new HashSet<>();
        mServers.add(new Server("http://10.111.17.139:5000", "Default server"));
        mProperties = new ArrayList<>();
    }

    /*package*/ int getId() {
        return mId;
    }

    public String getName() {
        return mProfileName;
    }

    /*package*/ Set<Server> getServers() {
        return mServers;
    }

    public TestProfile id(int id) {
        this.mId = id;
        return this;
    }

    @SuppressWarnings("all")
    private List<IntentProcessor> generateProcessors(JSONObject options) {
        int index = 0;
        List<IntentProcessor> processors = new ArrayList<>();
        processors.add(ProcessorFactory.buildProcessor(IntentProcessor.META));
        index++;
        for (String property : mProperties) {
            processors.add(ProcessorFactory.buildProcessor(propertyMap.get(property), options));
            Log.d("NFCTAG", "just added : " + property);
        }
        return processors;
    }

    /*package*/ TestProfile setup(JSONObject options) {
        mProcessors = generateProcessors(options);
        return this;
    }

    @Override
    public Iterator<IntentProcessor> iterator() {
        return new ProcessProfileIterator();
    }

    private class ProcessProfileIterator implements Iterator<IntentProcessor> {

        private int mIndex;

        private ProcessProfileIterator() {
            mIndex = 0;
        }

        @Override
        public boolean hasNext() {
            return mIndex < mProcessors.size();
        }

        @Override
        public IntentProcessor next() {
            return mProcessors.get(mIndex++);
        }

    }

}
