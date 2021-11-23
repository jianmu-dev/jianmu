package dev.jianmu.el;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @class ReflectTool
 * @description 反射工具类
 * @author Ethan Liu
 * @create 2021-02-20 22:05
*/
public class ReflectUntil {

    private static final List<Class<?>> convertPriority = new ArrayList<>();
    static {
        convertPriority.add(Integer.class);
        convertPriority.add(Long.class);
        convertPriority.add(Float.class);
        convertPriority.add(Double.class);
    }

    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();

    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<>();

    static {
        for (final Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperMap.entrySet()) {
            final Class<?> primitiveClass = entry.getKey();
            final Class<?> wrapperClass = entry.getValue();
            if (!primitiveClass.equals(wrapperClass)) {
                wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
            }
        }
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            Class<?> clazz = obj.getClass();
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    // TODO 当前的类型转换每次只转换一个参数类型，碰到多个参数类型需要转换的时候会有问题，后续需要反向匹配
    public static Object invokeMethod(final Object target, final String name, List<Object> args) {
        Class<?> clazz = target.getClass();

        // 参数类型匹配BigDecimal的情况
        Class<?>[] argTypes = ReflectUntil.getArgsType(args.toArray());
        Method method = ReflectUntil.getMatchingMethod(clazz, name, argTypes);
        if (null != method) {
            return invokeMethod(method, target, args.toArray());
        }
        // 参数类型不匹配的情况，按照convertPriority里定义的顺序进行转换
        for (final Class<?> cls : convertPriority) {
            for (int i = 0; i < args.size(); i++) {
                Object a = args.get(i);
                List<Object> newArgs = new ArrayList<>(args);
                if (a instanceof BigDecimal && cls.equals(Integer.class)) {
                    Integer newObj = ((BigDecimal) a).intValue();
                    newArgs.set(i, newObj);
                }
                if (a instanceof BigDecimal && cls.equals(Long.class)) {
                    Long newObj = ((BigDecimal) a).longValue();
                    newArgs.set(i, newObj);
                }
                if (a instanceof BigDecimal && cls.equals(Float.class)) {
                    Float newObj = ((BigDecimal) a).floatValue();
                    newArgs.set(i, newObj);
                }
                if (a instanceof BigDecimal && cls.equals(Double.class)) {
                    Double newObj = ((BigDecimal) a).doubleValue();
                    newArgs.set(i, newObj);
                }
                // 尝试用转换后的类型获取method
                argTypes = ReflectUntil.getArgsType(newArgs.toArray());
                method = ReflectUntil.getMatchingMethod(clazz, name, argTypes);
                if (null != method) {
                    return invokeMethod(method, target, newArgs.toArray());
                }
            }
        }
        throw new RuntimeException("对象: " + target + " 不支持方法： " + name);
    }

    public static Object invokeMethod(Method method, Object target, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method getMatchingMethod(final Class<?> clazz, final String name,
                                           final Class<?>... paramTypes) {
        final List<Method> methods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getName().equals(name))
                .collect(Collectors.toList());
        getAllSuperclasses(clazz).stream()
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .filter(method -> method.getName().equals(name))
                .forEach(methods::add);
        for (final Method method : methods) {
            if (Arrays.deepEquals(method.getParameterTypes(), paramTypes)) {
                return method;
            }
        }

        final TreeMap<Integer, List<Method>> candidates = new TreeMap<>();

        methods.stream()
                .filter(method -> isAssignable(paramTypes, method.getParameterTypes(), true))
                .forEach(method -> {
                    final int distance = distance(paramTypes, method.getParameterTypes());
                    final List<Method> candidatesAtDistance = candidates.computeIfAbsent(distance, k -> new ArrayList<>());
                    candidatesAtDistance.add(method);
                });
        if (candidates.isEmpty()) {
            return null;
        }

        final List<Method> bestCandidates = candidates.values().iterator().next();
        if (bestCandidates.size() == 1) {
            return bestCandidates.get(0);
        }
        throw new RuntimeException("类: " + clazz.getSimpleName() + " 找不到方法： " + name);
    }

    private static int distance(final Class<?>[] fromClassArray, final Class<?>[] toClassArray) {
        int answer = 0;

        if (!isAssignable(fromClassArray, toClassArray, true)) {
            return -1;
        }
        for (int offset = 0; offset < fromClassArray.length; offset++) {
            // Note InheritanceUtils.distance() uses different scoring system.
            final Class<?> aClass = fromClassArray[offset];
            final Class<?> toClass = toClassArray[offset];
            if (aClass == null || aClass.equals(toClass)) {
                continue;
            } else if (isAssignable(aClass, toClass, true)
                    && !isAssignable(aClass, toClass, false)) {
                answer++;
            } else {
                answer = answer + 2;
            }
        }

        return answer;
    }

    public static boolean isAssignable(Class<?>[] classArray, Class<?>[] toClassArray, final boolean autoboxing) {
        if (!(Array.getLength(classArray) == Array.getLength(toClassArray))) {
            return false;
        }
        if (classArray == null) {
            classArray = new Class[0];
        }
        if (toClassArray == null) {
            toClassArray = new Class[0];
        }
        for (int i = 0; i < classArray.length; i++) {
            if (!isAssignable(classArray[i], toClassArray[i], autoboxing)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAssignable(Class<?> cls, final Class<?> toClass, final boolean autoboxing) {
        if (toClass == null) {
            return false;
        }
        // have to check for null, as isAssignableFrom doesn't
        if (cls == null) {
            return !toClass.isPrimitive();
        }
        if (autoboxing) {
            // 包装类和基本类型转换判断
            if (cls.isPrimitive() && !toClass.isPrimitive()) {
                cls = primitiveToWrapper(cls);
                if (cls == null) {
                    return false;
                }
            }
            if (toClass.isPrimitive() && !cls.isPrimitive()) {
                cls = wrapperToPrimitive(cls);
                if (cls == null) {
                    return false;
                }
            }
        }
        // 类型是否相等判断
        if (cls.equals(toClass)) {
            return true;
        }
        // 如果都是原始类型，类型兼容判断
        if (cls.isPrimitive()) {
            if (!toClass.isPrimitive()) {
                return false;
            }
            if (Integer.TYPE.equals(cls)) {
                return Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Long.TYPE.equals(cls)) {
                return Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Boolean.TYPE.equals(cls)) {
                return false;
            }
            if (Double.TYPE.equals(cls)) {
                return false;
            }
            if (Float.TYPE.equals(cls)) {
                return Double.TYPE.equals(toClass);
            }
            if (Character.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass)
                        || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Short.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass)
                        || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Byte.TYPE.equals(cls)) {
                return Short.TYPE.equals(toClass)
                        || Integer.TYPE.equals(toClass)
                        || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            // should never get here
            return false;
        }
        return toClass.isAssignableFrom(cls);
    }

    public static Class<?> primitiveToWrapper(final Class<?> cls) {
        Class<?> convertedClass = cls;
        if (cls != null && cls.isPrimitive()) {
            convertedClass = primitiveWrapperMap.get(cls);
        }
        return convertedClass;
    }

    public static Class<?> wrapperToPrimitive(final Class<?> cls) {
        return wrapperPrimitiveMap.get(cls);
    }

    public static List<Class<?>> getAllSuperclasses(final Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        final List<Class<?>> classes = new ArrayList<>();
        Class<?> superclass = clazz.getSuperclass();
        while (superclass != null) {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return classes;
    }

    public static Class<?>[] getArgsType(Object... args) {
        Class<?>[] types = new Class[args.length];
        int i = 0;
        for (Object arg : args)
            types[i++] = (null == arg) ? Object.class : arg.getClass();
        return types;
    }
}
