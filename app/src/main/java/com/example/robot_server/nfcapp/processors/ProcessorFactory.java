package com.example.robot_server.nfcapp.processors;

import android.util.Log;

/**
 * Created by robot-server on 14.03.17.
 */

public class ProcessorFactory {

    private static final Class[] PROCESSORS = {MetaProcessor.class, ReadProcessor.class, WriteProcessor.class};

    public static IntentProcessor buildProcessor(int id) {
        try {
            for (Class c : PROCESSORS) {
                int cId = c.getDeclaredField("ID").getInt(null);
                if (c.getDeclaredField("ID").getInt(null) == id) {
                    return (IntentProcessor) c.newInstance();
                }
            }
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
