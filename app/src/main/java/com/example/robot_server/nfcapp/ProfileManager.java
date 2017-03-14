package com.example.robot_server.nfcapp;

import com.example.robot_server.nfcapp.processors.IntentProcessor;
import com.example.robot_server.nfcapp.processors.MetaProcessor;
import com.example.robot_server.nfcapp.processors.ProcessorFactory;
import com.example.robot_server.nfcapp.processors.ReadProcessor;
import com.example.robot_server.nfcapp.processors.WriteProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by robot-server on 14.03.17.
 */

public class ProfileManager {

    public static final String PROFILE_DETECT_ONLY = "Detect only";
    public static final String PROFILE_READ_ONLY = "Read only";
    public static final String PROFILE_WRITE_ONLY = "Write only";
    public static final String PROFILE_READ_WRITE = "Read write";

    private Map<Integer, String> mProfileNames;
    private Map<Integer, Object[]> mExtras;
    private List<Integer> mProcessors;
    private int mProfileSum;

    public ProfileManager() {
        mProfileNames = new HashMap<>();
        mProfileNames.put(0, PROFILE_DETECT_ONLY);
        mProfileNames.put(ReadProcessor.ID, PROFILE_READ_ONLY);
        mProfileNames.put(WriteProcessor.ID, PROFILE_WRITE_ONLY);
        mProfileNames.put(ReadProcessor.ID + WriteProcessor.ID, PROFILE_READ_WRITE);

        mProcessors = new ArrayList<>();
        mProcessors.add(MetaProcessor.ID);
        mProcessors.add(ReadProcessor.ID);
        mProcessors.add(WriteProcessor.ID);
        mProfileSum = 0;
        mExtras = new HashMap<>();
    }

    public void setRead(boolean read, Object... extras) {
        set(ReadProcessor.ID, read, extras);
    }

    public void setWrite(boolean write, Object... extras) {
        set(WriteProcessor.ID, write, extras);
    }

    public void set(int id, boolean doing, Object... extras) {
        if (doing) {
            if (!has(id)) {
                mProfileSum += id;
                mExtras.put(id, extras);
            }
        } else if (has(id)) {
            mProfileSum -= id;
            mExtras.put(id, null);
        }
    }

    public boolean has(int id) {
        if (id > mProfileSum) return false;
        if (id == mProfileSum) return true;
        int sum = mProfileSum;
        for (int i = mProcessors.size() - 1; mProcessors.get(i) > id; i--) {
            sum -= mProcessors.get(i);
        }
        return sum >= id;
    }

    public ProcessProfile buildProfile() {
        List<IntentProcessor> processors = new ArrayList<>();
        for (int i = mProcessors.size() - 1; i >= 0; i--) {
            int id = mProcessors.get(i);
            if (has(id)) {
                IntentProcessor processor = ProcessorFactory.buildProcessor(id);
                processor.receive(mExtras.get(id));
                processors.add(0, processor);
            }
        }
        processors.add(0, new MetaProcessor());
        return new ProcessProfile(mProfileSum, mProfileNames.get(mProfileSum), processors);
    }

}
