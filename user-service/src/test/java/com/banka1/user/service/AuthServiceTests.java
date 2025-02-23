package com.banka1.user.service;

import com.banka1.user.aspect.AuthAspect;
import com.banka1.user.aspect.Authorization;
import com.banka1.user.model.helper.Permission;
import com.banka1.user.model.helper.Position;
import io.jsonwebtoken.Claims;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {
    @Mock
    private IAuthService authService;
    @Mock
    private Claims claims;
    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private MethodSignature methodSignature;
    @Mock
    private Method method;
    @Mock
    private Authorization authorization;
    @InjectMocks
    private AuthAspect authAspect;

    @Test
    void authorizationTest_invalidToken() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization" });
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Bearer NevalidanToken" });

        authAspect.authorize(joinPoint);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void authorizationTest_blankToken() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization" });
        when(joinPoint.getArgs()).thenReturn(new Object[] { null });

        authAspect.authorize(joinPoint);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void authorizationTest_validTokenNoPerms() throws Throwable {
        when(claims.get("permissions", String[].class)).thenReturn(new String[] { Permission.CREATE_EMPLOYEE.getPermission() });
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization" });
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Bearer ValidanToken" });
        when(authService.parseToken(notNull(String.class))).thenReturn(claims);
        when(authorization.permissions()).thenReturn(new Permission[] { Permission.READ_EMPLOYEE });
        when(authorization.positions()).thenReturn(new Position[] { });
        when(method.getAnnotation(Authorization.class)).thenReturn(authorization);
        when(methodSignature.getMethod()).thenReturn(method);

        authAspect.authorize(joinPoint);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void authorizationTest_validTokenWrongPosition() throws Throwable {
        when(claims.get("permissions", String[].class)).thenReturn(new String[] { });
        when(claims.get("position", String.class)).thenReturn(Position.WORKER.getPosition());
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization" });
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Bearer ValidanToken" });
        when(authService.parseToken(notNull(String.class))).thenReturn(claims);
        when(authorization.permissions()).thenReturn(new Permission[] { });
        when(authorization.positions()).thenReturn(new Position[] { Position.MANAGER, Position.DIRECTOR });
        when(method.getAnnotation(Authorization.class)).thenReturn(authorization);
        when(methodSignature.getMethod()).thenReturn(method);

        authAspect.authorize(joinPoint);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void authorizationTest_validTokenWithInvalidIdFallback() throws Throwable {
        when(claims.get("permissions", String[].class)).thenReturn(new String[] { Permission.CREATE_EMPLOYEE.getPermission() });
        when(claims.get("id", Long.class)).thenReturn(1L);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "id" });
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Bearer ValidanToken", 2L });
        when(authService.parseToken(notNull(String.class))).thenReturn(claims);
        when(authorization.permissions()).thenReturn(new Permission[] { Permission.READ_EMPLOYEE });
        when(authorization.positions()).thenReturn(new Position[] { });
        when(authorization.allowIdFallback()).thenReturn(true);
        when(method.getAnnotation(Authorization.class)).thenReturn(authorization);
        when(methodSignature.getMethod()).thenReturn(method);

        authAspect.authorize(joinPoint);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void authorizationTest_validTokenWithValidPermsAndPositions() throws Throwable {
        when(claims.get("permissions", String[].class)).thenReturn(new String[] { Permission.CREATE_EMPLOYEE.getPermission(), Permission.READ_EMPLOYEE.getPermission() });
        when(claims.get("position", String.class)).thenReturn(Position.MANAGER.getPosition());
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization" });
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Bearer ValidanToken" });
        when(authService.parseToken(notNull(String.class))).thenReturn(claims);
        when(authorization.permissions()).thenReturn(new Permission[] { Permission.CREATE_EMPLOYEE });
        when(authorization.positions()).thenReturn(new Position[] { Position.MANAGER, Position.DIRECTOR });
        when(method.getAnnotation(Authorization.class)).thenReturn(authorization);
        when(methodSignature.getMethod()).thenReturn(method);

        authAspect.authorize(joinPoint);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void authorizationTest_validTokenWithValidIdFallback() throws Throwable {
        when(claims.get("permissions", String[].class)).thenReturn(new String[] { Permission.CREATE_EMPLOYEE.getPermission() });
        when(claims.get("id", Long.class)).thenReturn(1L);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "id" });
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Bearer ValidanToken", 1L });
        when(authService.parseToken(notNull(String.class))).thenReturn(claims);
        when(authorization.permissions()).thenReturn(new Permission[] { Permission.READ_EMPLOYEE });
        when(authorization.positions()).thenReturn(new Position[] { });
        when(authorization.allowIdFallback()).thenReturn(true);
        when(method.getAnnotation(Authorization.class)).thenReturn(authorization);
        when(methodSignature.getMethod()).thenReturn(method);

        authAspect.authorize(joinPoint);
        verify(joinPoint, times(1)).proceed();
    }
}
