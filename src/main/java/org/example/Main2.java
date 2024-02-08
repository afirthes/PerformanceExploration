package org.example;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class Main2 {
    public static void main(String[] args) throws Throwable {
        final int N = 5000000;
        final A a = new A();
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
        Adder adder = (Adder) callSite.getTarget().bindTo(a).invoke();
        long start = System.nanoTime();
        for (int i = 0; i < N; ++i) {
            adder.setA(i);
        }
        System.out.println(System.nanoTime() - start);
    }

}
