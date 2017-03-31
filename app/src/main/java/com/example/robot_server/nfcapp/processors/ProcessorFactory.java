package com.example.robot_server.nfcapp.processors;

import android.util.Log;

import java.lang.reflect.Field;

public class ProcessorFactory {

    private static final Class[] PROCESSORS = {MetaProcessor.class, ReadProcessor.class, WriteProcessor.class};

    public static IntentProcessor buildProcessor(int id) {
        try {
            for (Class c : PROCESSORS) {
                Field f = c.getDeclaredField("ID");
                f.setAccessible(true);
                if (f.getInt(null) == id) {
                    return (IntentProcessor) c.newInstance();
                }
            }
        } catch (NoSuchFieldException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
