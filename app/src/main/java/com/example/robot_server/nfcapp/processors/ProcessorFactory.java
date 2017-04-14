package com.example.robot_server.nfcapp.processors;

import com.example.robot_server.nfcapp.AppContext;
import com.example.robot_server.nfcapp.annotations.Inject;
import com.example.robot_server.nfcapp.utils.JsonUtils;

import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ProcessorFactory {

    /**
     * Finds the concrete {@link IntentProcessor} Class based on the given id.
     * By default it looks for the class name in the properties file, but fallsback to looking in
     * the processors package for a processor bearing the right name. Not ideal.
     *
     * @param id the {@link IntentProcessor} type identifier.
     * @return the concrete {@link IntentProcessor} class.
     * @throws ClassNotFoundException Can only occur if the value for the given id is incorrect
     *                                or missing frim the properties file and the processors package doesn't have a class named
     *                                {@param id} + 'Processor'.
     *                                Example: if the given id is 'read' and the props file doesn't have an entry for it,
     *                                the method will try to find a class with a FQDN of com.blabla.nfcapp.processors.ReadProcessor .
     */
    private static Class findClass(String id) throws ClassNotFoundException {
        String className = AppContext.getProperty(id);
        if (className != null) {
            return Class.forName(className);
        } else { //last resort, try and find a processor in the processors package
            String processorName = id.toLowerCase().
                    substring(0, 1).toUpperCase() +
                    id.toLowerCase().
                            substring(1) + "Processor";
            return Class.forName(ProcessorFactory.class.getPackage().getName() + "." + processorName);
        }
    }

    /**
     * Creates an instance of {@link IntentProcessor} based on the given id.
     *
     * @param id the IntentProcessor type identifier.
     * @return the newly created instance.
     */
    public static IntentProcessor buildProcessor(String id) {
        try {
            Class c = findClass(id);
            return (IntentProcessor) c.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Creates an instance of {@link IntentProcessor} based on the given id and injects dependencies if necessary.
     *
     * @param id      the IntentProcessor type identifier.
     * @param options the object containing the options for dependency injection.
     * @return the newly created instance.
     */
    public static IntentProcessor buildProcessor(String id, JSONObject options) {
        try {
            Class c = findClass(id);
            return buildProcessor(c, options);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
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
            Object option = JsonUtils.getNested(options, inject.name()); //support for nested keys, like 'identifiers.imei'
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
                targetField.setAccessible(false);
            } else if (!inject.nullable()) {
                throw new IllegalArgumentException("no value for dependency " + inject.name() + " provided");
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
