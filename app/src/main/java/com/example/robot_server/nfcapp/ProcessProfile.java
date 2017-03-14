package com.example.robot_server.nfcapp;

import com.example.robot_server.nfcapp.processors.IntentProcessor;

import java.util.Iterator;
import java.util.List;

/**
 * Created by robot-server on 14.03.17.
 */

public class ProcessProfile implements Iterable<IntentProcessor> {

    private int mId;
    private String mName;
    private List<IntentProcessor> mProcessors;

    public ProcessProfile(int id, String name, List<IntentProcessor> processors) {
        mId = id;
        mName = name;
        mProcessors = processors;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
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
