package com.example.robot_server.nfcapp.processors;

public class ProcessorFactory {

    private static final Class[] PROCESSORS = {MetaProcessor.class, ReadProcessor.class, WriteProcessor.class};

    public static IntentProcessor buildProcessor(int id) {
        try {
            for (Class c : PROCESSORS) {
                if (c.getDeclaredField("ID").getInt(null) == id) {
                    return (IntentProcessor) c.newInstance();
                }
            }
        } catch (NoSuchFieldException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
