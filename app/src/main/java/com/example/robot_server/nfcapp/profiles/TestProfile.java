package com.example.robot_server.nfcapp.profiles;

import com.example.robot_server.nfcapp.domain.Server;
import com.example.robot_server.nfcapp.domain.StringWrapper;
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

    public TestProfile() {
        readContent = new StringWrapper();
        writeContent = new StringWrapper();
        mServers = new HashSet<>();
        mServers.add(new Server("http://10.111.17.139:5000", "Default server"));
    }

    public int getId() {
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

    public TestProfile name(String name) {
        this.mName = name;
        return this;
    }

    public TestProfile read(boolean read) {
        this.read = read;
        return this;
    }

    public TestProfile write(boolean write) {
        this.write = write;
        return this;
    }

    public TestProfile readContent(StringWrapper readContent) {
        this.readContent = readContent;
        return this;
    }

    public TestProfile toWrite(StringWrapper writeContent) {
        this.writeContent = writeContent;
        return this;
    }

    public TestProfile servers(Set<Server> servers) {
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
            processors.get(index++).receive(readContent); // tell the processor where to store the read content.
        }
        if (write) {
            processors.add(ProcessorFactory.buildProcessor(IntentProcessor.WRITE));
            processors.get(index++).receive(writeContent); // tell the processor where to read the content to write from.
        }
        return processors;
    }

    public void setup() {
        mProcessors = generateProcessors();
    }

    public JSONObject toJson() {
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
