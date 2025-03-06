package com.banka1.banking.utils;

@FunctionalInterface
public interface ThrowingFunction<T, R> {
    R apply(T Arg) throws Throwable;
}
