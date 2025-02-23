package com.banka1.user.aspect;

import com.banka1.user.service.IAuthService;
import io.jsonwebtoken.Claims;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementacija logike middleware-a za autorizaciju i autentifikaciju.
 * <p>
 * Za detalje o korišćenju, videti {@link Authorization}. Metode ove klase se ne trebaju eksplicitno pozivati van testova.
 */
@Aspect
@Configuration
@EnableAspectJAutoProxy
public class AuthAspect {
    private final IAuthService authService;

    public AuthAspect(IAuthService authService) {
        this.authService = authService;
    }

    // Izvlači token iz prosledjenog autorizacionog parametra
    private String parseAuthToken(String[] params, Object[] args) {
        String token = null;
        for (int i = 0; i < params.length; i++) {
            if(args[i] == null)
                continue;

            if(params[i].toLowerCase().compareTo("authorization") == 0 && args[i].toString().startsWith("Bearer"))
                token = args[i].toString().split(" ")[1];
        }
        return token;
    }

    /**
     * Pri pozivanju kontroler metode, proverava da li je njoj prosledjen validan auth header koji sadrži potrebne permisije na osnovu date {@link Authorization} anotacije.
     * Ova metoda se ne treba eksplicitno pozivati van testova.
     */
    @Around("@annotation(com.banka1.user.aspect.Authorization)")
    public Object checkPermissions(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String token = parseAuthToken(methodSignature.getParameterNames(), joinPoint.getArgs());

        if(token == null) {
            // Token ne postoji, tj. korisnik nije ulogovan
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Claims claims = authService.parseToken(token);
        if(claims == null) {
            // Token postoji ali nije autentičan, verovatno je pokušaj neodobrenog pristupa
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Authorization authorization = method.getAnnotation(Authorization.class);

        Set<String> necessaryPermissions = Arrays.stream(authorization.permissions()).map(String::valueOf).collect(Collectors.toSet());
        Set<String> grantedPermissions = new HashSet<>(Arrays.asList(claims.get("permissions", String[].class)));

        // Moguće je da korisnik ima i više permisija nego što je potrebno
        if(grantedPermissions.containsAll(necessaryPermissions)) {
            return joinPoint.proceed();
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}