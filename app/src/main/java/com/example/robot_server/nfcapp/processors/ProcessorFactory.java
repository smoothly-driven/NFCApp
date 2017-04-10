package com.example.robot_server.nfcapp.processors;

import android.support.annotation.Nullable;

import com.example.robot_server.nfcapp.annotations.Inject;

import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ProcessorFactory {

    private static final Class[] PROCESSORS = {MetaProcessor.class, ReadProcessor.class, WriteProcessor.class};

    @Deprecated
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

    public static IntentProcessor buildProcessor(int id, JSONObject options) {
        try {
            Class c = findClass(id);
            if (c != null)
                return buildProcessor(c, options);
        } catch (NoSuchFieldException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static Class findClass(int id) throws NoSuchFieldException, IllegalAccessException {
        for (Class c : PROCESSORS) {
            Field f = c.getDeclaredField("ID");
            f.setAccessible(true);
            if (f.getInt(null) == id)
                return c;
        }
        return null;
    }

    private static IntentProcessor buildProcessor(Class c, JSONObject options) throws InstantiationException, IllegalAccessException {
        IntentProcessor processor = (IntentProcessor) c.newInstance();
        Set<Field> targets = findFieldsByAnnotation(c, Inject.class);
        for (Field target : targets) {
            Inject inject = target.getAnnotation(Inject.class);
            Object option = options.opt(inject.value());
            inject(target, inject, option, processor);
        }
        return processor;
    }

    private static void inject(Field target, Inject inject, Object option, Object object) {
        try {
            if (option != null) {
                target.setAccessible(true);
                target.set(object, option); //inject!
            } else if (!inject.nullable()) {
                throw new IllegalArgumentException("no value for " + inject.value() + " provided");
            }
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return null safe set
     */
    private static Set<Field> findFieldsByAnnotation(Class<?> classs, Class<? extends Annotation> ann) {
        Set<Field> set = new HashSet<>();
        Class<?> c = classs;
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(ann)) {
                    set.add(field);
                }
            }
            c = c.getSuperclass();
        }
        return set;
    }
}
