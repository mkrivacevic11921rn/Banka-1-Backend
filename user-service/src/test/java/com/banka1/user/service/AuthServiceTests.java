package com.banka1.user.service;

import com.banka1.user.aspect.AuthAspect;
import com.banka1.user.aspect.Authorization;
import com.banka1.user.model.helper.Permission;
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
        when(joinPoint.getArgs()).thenReturn(new String[] { "Bearer NevalidanToken" });

        authAspect.checkPermissions(joinPoint);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void authorizationTest_blankToken() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization" });
        when(joinPoint.getArgs()).thenReturn(new String[] { null });

        authAspect.checkPermissions(joinPoint);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void authorizationTest_validTokenNoPerms() throws Throwable {
        when(claims.get("permissions", String[].class)).thenReturn(new String[] { String.valueOf(Permission.CREATE_EMPLOYEE) });
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization" });
        when(joinPoint.getArgs()).thenReturn(new String[] { "Bearer ValidanToken" });
        when(authService.parseToken(notNull(String.class))).thenReturn(claims);
        when(authorization.permissions()).thenReturn(new String[] { String.valueOf(Permission.READ_EMPLOYEE) });
        when(method.getAnnotation(Authorization.class)).thenReturn(authorization);
        when(methodSignature.getMethod()).thenReturn(method);

        authAspect.checkPermissions(joinPoint);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void authorizationTest_completelyValidToken() throws Throwable {
        when(claims.get("permissions", String[].class)).thenReturn(new String[] { String.valueOf(Permission.CREATE_EMPLOYEE) });
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization" });
        when(joinPoint.getArgs()).thenReturn(new String[] { "Bearer ValidanToken" });
        when(authService.parseToken(notNull(String.class))).thenReturn(claims);
        when(authorization.permissions()).thenReturn(new String[] { String.valueOf(Permission.CREATE_EMPLOYEE) });
        when(method.getAnnotation(Authorization.class)).thenReturn(authorization);
        when(methodSignature.getMethod()).thenReturn(method);

        authAspect.checkPermissions(joinPoint);
        verify(joinPoint, times(1)).proceed();
    }
}
