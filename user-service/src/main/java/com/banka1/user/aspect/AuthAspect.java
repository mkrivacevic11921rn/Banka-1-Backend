package com.banka1.user.aspect;

import com.banka1.user.model.helper.Permission;
import com.banka1.user.model.helper.Position;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
     * Pri pozivanju kontroler metode, proverava da li je njoj prosledjen validan auth header koji sadrži potrebne permisije i pozicije (ili poklapajući id) na osnovu date {@link Authorization} anotacije.
     * Ova metoda se ne treba eksplicitno pozivati van testova.
     */
    @Around("@annotation(com.banka1.user.aspect.Authorization)")
    public Object authorize(ProceedingJoinPoint joinPoint) throws Throwable {
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

        Set<String> necessaryPermissions = Arrays.stream(authorization.permissions()).map(Permission::getPermission).collect(Collectors.toSet());
        Set<String> grantedPermissions = new HashSet<>(Arrays.asList(claims.get("permissions", String[].class)));

        Set<String> possiblePositions = Arrays.stream(authorization.positions()).map(Position::getPosition).collect(Collectors.toSet());

        // Moguće je da korisnik ima i više permisija nego što je potrebno
        if(grantedPermissions.containsAll(necessaryPermissions) && (possiblePositions.size() == 0 || possiblePositions.contains(claims.get("position", String.class)))) {
            return joinPoint.proceed();
        }

        if(authorization.allowIdFallback()) {
            OptionalInt maybeIdIndex = IntStream.range(0, methodSignature.getParameterNames().length).filter(i -> methodSignature.getParameterNames()[i].compareTo("id") == 0).findFirst();
            if(maybeIdIndex.isPresent()) {
                Long givenId = Long.parseLong(joinPoint.getArgs()[maybeIdIndex.getAsInt()].toString());
                if (Objects.equals(claims.get("id", Long.class), givenId)) {
                    return joinPoint.proceed();
                }
            }
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}