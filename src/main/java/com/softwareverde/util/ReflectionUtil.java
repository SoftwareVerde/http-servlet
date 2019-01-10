package com.softwareverde.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtil {
    protected ReflectionUtil() { }

    @SuppressWarnings("unchecked")
    public static <T> void setValue(final Object object, final String memberName, final T newValue) {
        RuntimeException lastException = null;

        Class<?> clazz = object.getClass();
        do {
            try {
                final Field field = clazz.getDeclaredField(memberName);
                field.setAccessible(true);
                field.set(object, newValue);
                return;
            }
            catch (final NoSuchFieldException e) {
                lastException = new RuntimeException("Invalid member name found in mock: "+ object.getClass().getSimpleName() +"."+ memberName);
            }
            catch (final IllegalAccessException e) {
                lastException = new RuntimeException("Unable to access member in mock: "+ object.getClass().getSimpleName() +"."+ memberName);
            }
        } while ((clazz = clazz.getSuperclass()) != null);

        throw lastException;
    }

    public static <T> T getValue(final Object object, final String memberName) {
        return getValue(object.getClass(), object, memberName);
    }

    public static <T> T getStaticValue(final Class objectClass, final String memberName) {
        return getValue(objectClass, null, memberName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(final Class objectClass, final Object object, final String memberName) {
        RuntimeException lastException = null;

        Class<?> clazz = objectClass;
        do {
            try {
                final Field field = clazz.getDeclaredField(memberName);
                field.setAccessible(true);
                return (T) field.get(object);
            }
            catch (final NoSuchFieldException e) {
                lastException = new RuntimeException("Invalid member name found in mock: " + objectClass.getSimpleName() + "." + memberName);
            }
            catch (final IllegalAccessException e) {
                lastException = new RuntimeException("Unable to access member in mock: " + objectClass.getSimpleName() + "." + memberName);
            }
        } while ((clazz = clazz.getSuperclass()) != null);

        throw lastException;
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(final Object object, final String methodName, final Object... params) {
        RuntimeException lastException = null;

        final Class[] parameterTypes = new Class[params.length];
        for (int i=0; i<params.length; ++i) {
            final Object param = params[i];
            parameterTypes[i] = param.getClass();
        }

        Class<?> clazz = object.getClass();
        do {
            try {
                final Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return (T) method.invoke(object, params);
            }
            catch (final NoSuchMethodException e) {
                lastException =  new RuntimeException("Invalid method name found in mock: "+ object.getClass().getSimpleName() +"::"+ methodName);
            }
            catch (final IllegalAccessException e) {
                lastException =  new RuntimeException("Unable to access method in mock: "+ object.getClass().getSimpleName() +"::"+ methodName);
            }
            catch (final InvocationTargetException e) {
                lastException =  new RuntimeException("Exception during execution of: "+ object.getClass().getSimpleName() +"::"+ methodName);
            }
        } while ((clazz = clazz.getSuperclass()) != null);

        throw lastException;
    }

    public static List<String> getFieldNames(final Object object) {
        RuntimeException lastException = null;

        final List<String> fieldNames = new ArrayList<String>();

        Class<?> clazz = object.getClass();
        do {
            try {
                final Field[] fields = clazz.getDeclaredFields();
                for (final Field field : fields) {
                    fieldNames.add(field.getName());
                }
            }
            catch (final RuntimeException exception) {
                lastException = exception;
            }
        } while ((clazz = clazz.getSuperclass()) != null);

        if (lastException != null) {
            throw lastException;
        }

        return fieldNames;
    }
}
