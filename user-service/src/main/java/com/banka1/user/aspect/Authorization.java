package com.banka1.user.aspect;

import com.banka1.user.model.helper.Permission;
import com.banka1.user.model.helper.Position;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorization {
    Permission[] permissions() default { };
    Position[] positions() default { };
    boolean allowIdFallback() default false;
}