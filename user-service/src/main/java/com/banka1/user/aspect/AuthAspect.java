package com.banka1.user.aspect;

import com.banka1.user.service.IAuthService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

@Aspect
@Configuration
@EnableAspectJAutoProxy
public class AuthAspect {
    @Value("${oauth.jwt.secret}")
    private String secret;
    private final IAuthService authService;

    public AuthAspect(IAuthService authService) {
        this.authService = authService;
    }

    private String parseAuthToken(String[] params, Object[] args) {
        String token = null;
        for (int i = 0; i < params.length; i++) {
            if(args[i] == null)
                continue;

            if(params[i].compareTo("authorization") == 0 && args[i].toString().startsWith("Bearer"))
                token = args[i].toString().split(" ")[1];
        }
        return token;
    }

    @Around("@annotation(com.banka1.user.aspect.Authorization)")
    public Object checkPermissions(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String token = parseAuthToken(methodSignature.getParameterNames(), joinPoint.getArgs());

        if(token == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Claims claims = authService.parseToken(token);
        if(claims == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Authorization authorization = method.getAnnotation(Authorization.class);

        if(new HashSet<>(Arrays.asList(claims.get("permissions", String[].class))).equals(new HashSet<>(Arrays.asList(authorization.permissions())))) {
            return joinPoint.proceed();
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}