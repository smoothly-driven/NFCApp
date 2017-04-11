package com.example.robot_server.nfcapp.processors;

import com.example.robot_server.nfcapp.annotations.Inject;

import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ProcessorFactory {

    private static final Class[] PROCESSORS = {MetaProcessor.class, ReadProcessor.class, WriteProcessor.class};

    /**
     * Finds the concrete {@link IntentProcessor} Class based on the given id.
     *
     * @param id the {@link IntentProcessor} type identifier { {@link MetaProcessor}, {@link ReadProcessor}, {@link WriteProcessor} }.
     * @return the concrete {@link IntentProcessor} class.
     * @throws NoSuchFieldException   Should never occur.
     * @throws IllegalAccessException Should never occur.
     */
    private static Class findClass(int id) throws NoSuchFieldException, IllegalAccessException {
        for (Class c : PROCESSORS) {
            Field f = c.getDeclaredField("ID");
            f.setAccessible(true);
            if (f.getInt(null) == id)
                return c;
        }
        return null;
    }

    /**
     * Creates an instance of {@link IntentProcessor} based on the given id.
     *
     * @param id the IntentProcessor type identifier { {@link MetaProcessor}, {@link ReadProcessor}, {@link WriteProcessor} }.
     * @return the newly created instance.
     */
    public static IntentProcessor buildProcessor(int id) {
        try {
            Class c = findClass(id);
            if (c != null) {
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

    /**
     * Creates an instance of {@link IntentProcessor} based on the given id and injects dependencies if necessary.
     *
     * @param id      the IntentProcessor type identifier { {@link MetaProcessor}, {@link ReadProcessor}, {@link WriteProcessor} }.
     * @param options the object containing the options for dependency injection.
     * @return the newly created instance.
     */
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

    /**
     * Creates an instance of {@link IntentProcessor} based on the given concrete class and inject dependencies if necessary.
     *
     * @param c       the class of the concrete implementation of {@link IntentProcessor} to instantiate.
     * @param options the object containing the options for dependency injection.
     * @return the newly created instance.
     * @throws InstantiationException shouldn't occur, ever.
     * @throws IllegalAccessException shouldn't occur, ever.
     */
    private static IntentProcessor buildProcessor(Class c, JSONObject options) throws InstantiationException, IllegalAccessException {
        IntentProcessor processor = (IntentProcessor) c.newInstance();
        Set<Field> targets = findFieldsByAnnotation(c, Inject.class);
        for (Field target : targets) {
            Inject inject = target.getAnnotation(Inject.class);
            Object option = options.opt(inject.name());
            inject(target, inject, option, processor);
        }
        return processor;
    }

    /**
     * Injects the given {@param dependency} into the given {@param instance}.
     *
     * @param targetField the field to receive the {@param dependency}.
     * @param inject      the field's Inject annotation properties.
     * @param dependency  the value to inject.
     * @param instance    the instance who's field receives the {@param dependency}.
     */
    private static void inject(Field targetField, Inject inject, Object dependency, Object instance) {
        try {
            if (dependency != null) {
                targetField.setAccessible(true);
                Class targetType = targetField.getType();
                targetField.set(instance, targetType.cast(dependency)); //inject!
            } else if (!inject.nullable()) {
                throw new IllegalArgumentException("no value for " + inject.name() + " provided");
            }
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Finds the fields of the given {@param classs} bearing the given {@param annotation}.
     *
     * @param classs     the class to inspect.
     * @param annotation the annotation to look for.
     * @return a set containing the fields found.
     */
    private static Set<Field> findFieldsByAnnotation(Class<?> classs, Class<? extends Annotation> annotation) {
        Set<Field> set = new HashSet<>();
        Class<?> c = classs;
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotation)) {
                    set.add(field);
                }
            }
            c = c.getSuperclass();
        }
        return set;
    }
}
