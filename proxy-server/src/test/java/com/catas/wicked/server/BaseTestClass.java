package com.catas.wicked.server;

import java.lang.reflect.Field;

public class BaseTestClass {

    public static void setPrivateField(Object targetObject, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = targetObject.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, value);
    }
}
