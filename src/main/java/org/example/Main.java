package org.example;


import com.github.romanqed.jeflect.lambdas.LambdaFactory;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class Main {

    static A a = new A();
    private static long start;
    private static ArrayList<Long> results = new ArrayList<>();

    public static void main(String[] args) throws Throwable {
        start = System.nanoTime();
        for(int i=0; i < 1_000_000; i++) {
            a.setA(i);
        }
        results.add(System.nanoTime() - start);

        start = System.nanoTime();
        for(int i=0; i < 1_000_000; i++) {
            try {
                var method = A.class.getMethod("setA", Integer.class);
                method.invoke(a, i);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        results.add(System.nanoTime() - start);

        Method method = A.class.getDeclaredMethod("setA", Integer.class);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "setA",
                MethodType.methodType(Adder.class, A.class),
                MethodType.methodType(void.class, Integer.class),
                lookup.unreflect(method),
                MethodType.methodType(void.class, Integer.class)
        );
        var handler = callSite.getTarget();
        start = System.nanoTime();
        for(int i=0; i < 1_000_000; i++) {
            Adder adder = (Adder) handler.bindTo(a).invoke();
            adder.setA(i);
        }
        results.add(System.nanoTime() - start);


        var mthd = A.class.getMethod("setA", Integer.class);
        var factory = new LambdaFactory();
        var lambda = factory.packMethod(mthd);

        start = System.nanoTime();
        for(int i=0; i < 1_000_000; i++) {
            lambda.invoke(a, new Object[]{1});
        }
        results.add(System.nanoTime() - start);

        System.out.println(results);

        System.out.println("Difference " + (results.get(1) / results.get(0)));
        System.out.println("Difference " + (results.get(2) / results.get(0)));
        System.out.println("Difference " + (results.get(3) / results.get(0)));
    }
}