package com.example.robot_server.nfcapp.domain;

import android.util.Log;

import com.example.robot_server.nfcapp.processors.IntentProcessor;
import com.example.robot_server.nfcapp.processors.ProcessorFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TestProfile implements Iterable<IntentProcessor> {

    private boolean read;
    private boolean write;
    private StringWrapper readContent;
    private StringWrapper writeContent;
    private Set<Server> mServers;
    private int mId;
    private String mName;
    private List<IntentProcessor> mProcessors;

    /*package*/ TestProfile() {
        mName = "";
        readContent = new StringWrapper();
        writeContent = new StringWrapper();
        mServers = new HashSet<>();
        mServers.add(new Server("http://10.111.17.139:5000", "Default server"));
    }

    /*package*/ int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public boolean getRead() {
        return read;
    }

    public boolean getWrite() {
        return write;
    }

    public String getReadContent() {
        return readContent.get();
    }

    public String getWriteContent() {
        return writeContent.get();
    }

    public Set<Server> getServers() {
        return mServers;
    }

    public TestProfile id(int id) {
        this.mId = id;
        return this;
    }

    /*package*/ TestProfile name(String name) {
        this.mName = name;
        return this;
    }

    /*package*/ TestProfile read(boolean read) {
        this.read = read;
        return this;
    }

    /*package*/ TestProfile write(boolean write) {
        this.write = write;
        return this;
    }

    /*package*/ TestProfile readContent(StringWrapper readContent) {
        this.readContent = readContent;
        return this;
    }

    /*package*/ TestProfile toWrite(StringWrapper writeContent) {
        this.writeContent = writeContent;
        return this;
    }

    /*package*/ TestProfile servers(Set<Server> servers) {
        this.mServers = servers;
        return this;
    }

    @SuppressWarnings("all")
    private List<IntentProcessor> generateProcessors() {
        int index = 0;
        List<IntentProcessor> processors = new ArrayList<>();
        processors.add(ProcessorFactory.buildProcessor(IntentProcessor.META));
        index++;
        if (read) {
            processors.add(ProcessorFactory.buildProcessor(IntentProcessor.READ));
            Log.d("NFCTAG", "just added : " + processors.get(index));
            processors.get(index++).receive(readContent); // tell the processor where to store the read content.
        }
        if (write) {
            processors.add(ProcessorFactory.buildProcessor(IntentProcessor.WRITE));
            processors.get(index++).receive(writeContent); // tell the processor where to read the content to write from.
        }
        return processors;
    }

    /*package*/ void setup() {
        mProcessors = generateProcessors();
    }

    /*package*/ JSONObject toJson() {
        JSONObject json = new JSONObject();
        JSONArray servers = new JSONArray();
        try {
            json.put("id", mId);
            json.put("name", mName);
            json.put("read", read);
            json.put("write", write);
            json.put("toWrite", writeContent.get());
            for (Server server : mServers) {
                servers.put(server.toJson());
            }
            json.put("servers", servers);
        } catch (org.json.JSONException ex) {
            ex.printStackTrace();
            return null; //not great
        }
        return json;
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
