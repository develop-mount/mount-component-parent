package icu.develop.l2cache.utils;

import java.lang.reflect.*;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/10 11:30
 */
public class TypeUtils {

    public static Class<?> getClass(Type type) {
        if (type == null) {
            return null;
        } else if (type.getClass() == Class.class) {
            return (Class)type;
        } else if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType)type).getRawType());
        } else if (type instanceof TypeVariable) {
            Type boundType = ((TypeVariable)type).getBounds()[0];
            return boundType instanceof Class ? (Class)boundType : getClass(boundType);
        } else {
            if (type instanceof WildcardType) {
                Type[] upperBounds = ((WildcardType)type).getUpperBounds();
                if (upperBounds.length == 1) {
                    return getClass(upperBounds[0]);
                }
            }

            if (type instanceof GenericArrayType) {
                GenericArrayType genericArrayType = (GenericArrayType)type;
                Type componentType = genericArrayType.getGenericComponentType();
                Class<?> componentClass = getClass(componentType);
                return getArrayClass(componentClass);
            } else {
                return Object.class;
            }
        }
    }

    public static Class<?> getArrayClass(Class componentClass) {
        if (componentClass == Integer.TYPE) {
            return int[].class;
        } else if (componentClass == Byte.TYPE) {
            return byte[].class;
        } else if (componentClass == Short.TYPE) {
            return short[].class;
        } else if (componentClass == Long.TYPE) {
            return long[].class;
        } else if (componentClass == String.class) {
            return String[].class;
        } else {
            return componentClass == Object.class ? Object[].class : Array.newInstance(componentClass, 1).getClass();
        }
    }

    public static Class<?> getMapping(Type type) {
        if (type == null) {
            return null;
        } else if (type.getClass() == Class.class) {
            return (Class)type;
        } else if (type instanceof ParameterizedType) {
            return getMapping(((ParameterizedType)type).getRawType());
        } else {
            Type genericComponentType;
            if (type instanceof TypeVariable) {
                genericComponentType = ((TypeVariable)type).getBounds()[0];
                return genericComponentType instanceof Class ? (Class)genericComponentType : getMapping(genericComponentType);
            } else {
                if (type instanceof WildcardType) {
                    Type[] upperBounds = ((WildcardType)type).getUpperBounds();
                    if (upperBounds.length == 1) {
                        return getMapping(upperBounds[0]);
                    }
                }

                if (type instanceof GenericArrayType) {
                    genericComponentType = ((GenericArrayType)type).getGenericComponentType();
                    Class<?> componentClass = getClass(genericComponentType);
                    return getArrayClass(componentClass);
                } else {
                    return Object.class;
                }
            }
        }
    }
}
