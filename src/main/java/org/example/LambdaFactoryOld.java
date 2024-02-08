package org.example;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LambdaFactoryOld {
    public static void main(String[] args) throws Throwable {
        LambdaFactoryOld instance = new LambdaFactoryOld();

        Method runnableMethod = LambdaFactoryOld.class.getMethod("likeRunnable");
        Function<LambdaFactoryOld, Runnable> runnableFactory = createLambdaFactory(Runnable.class, runnableMethod);
        Runnable runnable = runnableFactory.apply(instance);
        runnable.run();

        Method consumerMethod = LambdaFactoryOld.class.getMethod("likeConsumer", Object.class);
        Function<LambdaFactoryOld, Consumer<Object>> consumerFactory = LambdaFactoryOld.createLambdaFactory(Consumer.class, consumerMethod);
        Consumer<Object> consumer = consumerFactory.apply(instance);
        consumer.accept("Value of consumer");

        Method supplierMethod = LambdaFactoryOld.class.getMethod("likeSupplier");
        Function<LambdaFactoryOld, Supplier<Object>> supplierFactory = LambdaFactoryOld.createLambdaFactory(Supplier.class, supplierMethod);
        Supplier<Object> supplier = supplierFactory.apply(instance);
        System.out.println(supplier.get());

        Method predicateMethod = LambdaFactoryOld.class.getMethod("likePredicate", Object.class);
        Function<LambdaFactoryOld, Predicate<Object>> predicateFactory = LambdaFactoryOld.createLambdaFactory(Predicate.class, predicateMethod);
        Predicate<Object> predicate = predicateFactory.apply(instance);
        System.out.println(predicate.test(null));

        Method functionMethod = LambdaFactoryOld.class.getMethod("likeFunction", Object.class);
        Function<LambdaFactoryOld, Function<Object, Object>> functionFactory = LambdaFactoryOld.createLambdaFactory(Function.class, functionMethod);
        Function<Object, Object> function = functionFactory.apply(instance);
        System.out.println(function.apply("Value of function"));

        Method biFunctionMethod = LambdaFactoryOld.class.getMethod("likeBiFunction", Object.class, Object.class);
        Function<LambdaFactoryOld, BiFunction<Object, Object, Object>> biFunctionFactory = LambdaFactoryOld.createLambdaFactory(BiFunction.class, biFunctionMethod);
        BiFunction<Object, Object, Object> biFunction = biFunctionFactory.apply(instance);
        System.out.println(biFunction.apply("Value of bi-function", null));
    }

    public static <T, L> Function<T, L> createLambdaFactory(Class<? super L> lambdaType, Method implMethod) throws Throwable {
        Method lambdaMethod = findLambdaMethod(lambdaType);
        MethodType lambdaMethodType = MethodType.methodType(lambdaMethod.getReturnType(), lambdaMethod.getParameterTypes());

        Class<?> implType = implMethod.getDeclaringClass();

        MethodHandles.Lookup lookup = MethodHandles.lookup().in(implType);
        MethodType implMethodType = MethodType.methodType(implMethod.getReturnType(), implMethod.getParameterTypes());
        MethodHandle implMethodHandle = lookup.findVirtual(implType, implMethod.getName(), implMethodType);

        MethodType invokedMethodType = MethodType.methodType(lambdaType, implType);

        CallSite metafactory = LambdaMetafactory.metafactory(
                lookup,
                lambdaMethod.getName(), invokedMethodType, lambdaMethodType,
                implMethodHandle, implMethodType);

        MethodHandle factory = metafactory.getTarget();
        return instance -> {
            try {
                @SuppressWarnings("unchecked")
                L lambda = (L) factory.invoke(instance);
                return lambda;
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    public static Method findLambdaMethod(Class<?> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException("This must be interface: " + type);
        }
        Method[] methods = getAllMethods(type);
        if (methods.length == 0) {
            throw new IllegalArgumentException("No methods in: " + type.getName());
        }
        Method targetMethod = null;
        for (Method method : methods) {
            if (isInterfaceMethod(method)) {
                if (targetMethod != null) {
                    throw new IllegalArgumentException("This isn't functional interface: " + type.getName());
                }
                targetMethod = method;
            }
        }
        if (targetMethod == null) {
            throw new IllegalArgumentException("No method in: " + type.getName());
        }
        return targetMethod;
    }

    public static Method[] getAllMethods(Class<?> type) {
        LinkedList<Method> result = new LinkedList<>();
        Class<?> current = type;
        do {
            result.addAll(Arrays.asList(current.getMethods()));
        } while ((current = current.getSuperclass()) != null);
        return result.toArray(new Method[0]);
    }

    public static boolean isInterfaceMethod(Method method) {
        return !method.isDefault() && Modifier.isAbstract(method.getModifiers());
    }

    // Lambda implementation methods

    public void likeRunnable() {
        System.out.println("Run!");
    }

    public void likeConsumer(Object obj) {
        System.out.println("Consume: " + obj);
    }

    public Object likeSupplier() {
        return "Value of supplier";
    }

    public boolean likePredicate(Object ignore) {
        return true;
    }

    public Object likeFunction(Object o) {
        return o;
    }

    public Object likeBiFunction(Object o, Object ignore) {
        return o;
    }
}
