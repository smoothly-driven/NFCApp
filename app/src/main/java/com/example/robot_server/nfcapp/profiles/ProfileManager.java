package com.example.robot_server.nfcapp.profiles;

import com.example.robot_server.nfcapp.processors.IntentProcessor;
import com.example.robot_server.nfcapp.processors.ProcessorFactory;

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
        mProfileNames.put(IntentProcessor.READ, PROFILE_READ_ONLY);
        mProfileNames.put(IntentProcessor.WRITE, PROFILE_WRITE_ONLY);
        mProfileNames.put(IntentProcessor.READ + IntentProcessor.WRITE, PROFILE_READ_WRITE);

        mProcessors = new ArrayList<>();
        mProcessors.add(IntentProcessor.META);
        mProcessors.add(IntentProcessor.READ);
        mProcessors.add(IntentProcessor.WRITE);
        mProfileSum = 0;
        mExtras = new HashMap<>();
    }

    public void setRead(boolean read, Object... extras) {
        set(IntentProcessor.READ, read, extras);
    }

    public void setWrite(boolean write, Object... extras) {
        set(IntentProcessor.WRITE, write, extras);
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

    public int getProfileId() {
        return mProfileSum;
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
        processors.add(0, ProcessorFactory.buildProcessor(IntentProcessor.META)); //always add the meta processor
        return new ProcessProfile(mProfileSum, mProfileNames.get(mProfileSum), processors);
    }

}
