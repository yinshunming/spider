package com.nju.spider.bean;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class A {
    private int m;

    public A() {
    }

    public static void main(String [] args) {
        A a = new A();
        a.setM(12);
    }
}
