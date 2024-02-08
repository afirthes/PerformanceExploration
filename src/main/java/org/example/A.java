package org.example;

import net.bytebuddy.build.CachedReturnPlugin;

import java.util.Random;
import java.util.function.Consumer;

@MyMark
public class A implements Adder {

    private Integer a;

    @CachedReturnPlugin.Enhance
    public Integer random() {
        return new Random().nextInt();
    }

    public Integer getA() {
        return a;
    }

    public void setA(Integer a) {
        this.a = a;
    }


}
