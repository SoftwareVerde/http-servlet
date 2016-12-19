package com.softwareverde.test.utils;

import java.lang.reflect.Field;

public class TestUtil {
    public static <T> void setValue(final Object self, final String memberName, final T newValue) {
        final Class<?> clazz = self.getClass().getSuperclass();
        final Field field;
        try {
            field = clazz.getDeclaredField(memberName);
            field.setAccessible(true);
            field.set(self, newValue);
        }
        catch (final NoSuchFieldException e) {
            throw new RuntimeException("Invalid member name found in mock: "+ self.getClass().getSimpleName() +"."+ memberName);
        }
        catch (final IllegalAccessException e) {
            throw new RuntimeException("Unable to access member in mock: "+ self.getClass().getSimpleName() +"."+ memberName);
        }
    }

    public static <T> T getValue(final Object self, final String memberName) {
        final Class<?> clazz = self.getClass().getSuperclass();
        final Field field;
        try {
            field = clazz.getDeclaredField(memberName);
            field.setAccessible(true);
            return (T) field.get(self);
        }
        catch (final NoSuchFieldException e) {
            throw new RuntimeException("Invalid member name found in mock: "+ self.getClass().getSimpleName() +"."+ memberName);
        }
        catch (final IllegalAccessException e) {
            throw new RuntimeException("Unable to access member in mock: "+ self.getClass().getSimpleName() +"."+ memberName);
        }
    }
}
