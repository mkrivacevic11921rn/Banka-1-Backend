package com.banka1.banking.service;

import com.banka1.banking.aspect.AccountAuthorization;
import com.banka1.banking.aspect.AuthAspect;
import com.banka1.banking.aspect.CardAuthorization;
import com.banka1.banking.aspect.LoanAuthorization;
import com.banka1.banking.models.Account;
import com.banka1.banking.models.Card;
import com.banka1.banking.models.Loan;
import com.banka1.banking.services.AccountService;
import com.banka1.banking.services.CardService;
import com.banka1.banking.services.LoanService;
import com.banka1.banking.services.implementation.AuthService;
import com.banka1.common.model.Position;
import io.jsonwebtoken.Claims;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Base64;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {
    @Mock
    private AuthService authService;
    @Mock
    private AccountService accountService;
    @Mock
    private LoanService loanService;
    @Mock
    private CardService cardService;
    @InjectMocks
    private AuthAspect authAspect;
    @Mock
    private Claims claims;
    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private MethodSignature methodSignature;
    @Mock
    private Method method;
    @Mock
    private AccountAuthorization accountAuthorization;
    @Mock
    private LoanAuthorization loanAuthorization;
    @Mock
    private CardAuthorization cardAuthorization;
    @Mock
    private Account account;
    @Mock
    private Card card;
    @Mock
    private Loan loan;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(authService, "secret", Base64.getEncoder().encodeToString("TEST_SECRET_12345678_ABC!_00defgh".getBytes()));
        ReflectionTestUtils.setField(authService, "jwtExpiration", 100000);
    }

    @Test
    void authorizationTest_invalidToken() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization" });
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Bearer NevalidanToken" });

        authAspect.authorizeAccountAction(joinPoint);
        authAspect.authorizeCardAction(joinPoint);
        authAspect.authorizeLoanAction(joinPoint);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void authorizationTest_blankToken() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization" });
        when(joinPoint.getArgs()).thenReturn(new Object[] { null });

        authAspect.authorizeAccountAction(joinPoint);
        authAspect.authorizeCardAction(joinPoint);
        authAspect.authorizeLoanAction(joinPoint);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void authorizationTest_validTokenNoAccountAccess() throws Throwable {
        when(claims.get("id", Long.class)).thenReturn(1L);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Bearer ValidanToken", 1L });

        when(authService.parseToken(notNull(String.class))).thenReturn(claims);

        when(accountService.findById(1L)).thenReturn(account);
        //when(loanService.findById("1")).thenReturn(loan);
        when(cardService.findById(1L)).thenReturn(card);

        when(account.getOwnerID()).thenReturn(2L);
        //when(loan.getAccount()).thenReturn(account);
        when(card.getAccount()).thenReturn(account);

        when(accountAuthorization.customerOnlyOperation()).thenReturn(true);
        when(cardAuthorization.customerOnlyOperation()).thenReturn(true);
        //when(loanAuthorization.customerOnlyOperation()).thenReturn(true);
        when(accountAuthorization.disallowAdminFallback()).thenReturn(true);
        when(cardAuthorization.disallowAdminFallback()).thenReturn(true);
        //when(loanAuthorization.disallowAdminFallback()).thenReturn(true);

        when(method.getAnnotation(AccountAuthorization.class)).thenReturn(accountAuthorization);
        when(method.getAnnotation(CardAuthorization.class)).thenReturn(cardAuthorization);
        //when(method.getAnnotation(LoanAuthorization.class)).thenReturn(loanAuthorization);
        when(methodSignature.getParameterTypes()).thenReturn(new Class[] { String.class, Long.class });

        when(methodSignature.getMethod()).thenReturn(method);
        when(authService.getToken(notNull(String.class))).thenReturn("ValidanToken");

        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "accountId" });
        authAspect.authorizeAccountAction(joinPoint);

        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "cardId" });
        authAspect.authorizeCardAction(joinPoint);

        //when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "loanId" });
        //authAspect.authorizeLoanAction(joinPoint);

        verify(joinPoint, never()).proceed();
    }

    @Test
    void authorizationTest_validTokenEmployeeAccess() throws Throwable {
        when(claims.get("position", String.class)).thenReturn(Position.WORKER.toString());

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Bearer ValidanToken", 1L });

        when(authService.parseToken(notNull(String.class))).thenReturn(claims);

        when(accountAuthorization.customerOnlyOperation()).thenReturn(false);
        when(cardAuthorization.customerOnlyOperation()).thenReturn(false);
        //when(loanAuthorization.customerOnlyOperation()).thenReturn(false);

        when(method.getAnnotation(AccountAuthorization.class)).thenReturn(accountAuthorization);
        when(method.getAnnotation(CardAuthorization.class)).thenReturn(cardAuthorization);
        //when(method.getAnnotation(LoanAuthorization.class)).thenReturn(loanAuthorization);
        //when(methodSignature.getParameterTypes()).thenReturn(new Class[] { String.class, Long.class });

        when(methodSignature.getMethod()).thenReturn(method);
        when(authService.getToken(notNull(String.class))).thenReturn("ValidanToken");

        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "accountId" });
        authAspect.authorizeAccountAction(joinPoint);

        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "cardId" });
        authAspect.authorizeCardAction(joinPoint);

        //when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "loanId" });
        //authAspect.authorizeLoanAction(joinPoint);

        verify(joinPoint, times(2)).proceed();
    }

    @Test
    void authorizationTest_validTokenAdminAccess() throws Throwable {
        when(claims.get("id", Long.class)).thenReturn(1L);
        when(claims.get("isAdmin", Boolean.class)).thenReturn(true);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Bearer ValidanToken", 1L });

        when(authService.parseToken(notNull(String.class))).thenReturn(claims);

        when(accountService.findById(1L)).thenReturn(account);
        //when(loanService.findById("1")).thenReturn(loan);
        when(cardService.findById(1L)).thenReturn(card);

        when(account.getOwnerID()).thenReturn(2L);
        //when(loan.getAccount()).thenReturn(account);
        when(card.getAccount()).thenReturn(account);

        when(accountAuthorization.customerOnlyOperation()).thenReturn(true);
        when(cardAuthorization.customerOnlyOperation()).thenReturn(true);
        //when(loanAuthorization.customerOnlyOperation()).thenReturn(true);
        when(accountAuthorization.disallowAdminFallback()).thenReturn(false);
        when(cardAuthorization.disallowAdminFallback()).thenReturn(false);
        //when(loanAuthorization.disallowAdminFallback()).thenReturn(false);

        when(method.getAnnotation(AccountAuthorization.class)).thenReturn(accountAuthorization);
        when(method.getAnnotation(CardAuthorization.class)).thenReturn(cardAuthorization);
        //when(method.getAnnotation(LoanAuthorization.class)).thenReturn(loanAuthorization);
        when(methodSignature.getParameterTypes()).thenReturn(new Class[] { String.class, Long.class });

        when(methodSignature.getMethod()).thenReturn(method);
        when(authService.getToken(notNull(String.class))).thenReturn("ValidanToken");

        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "accountId" });
        authAspect.authorizeAccountAction(joinPoint);

        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "cardId" });
        authAspect.authorizeCardAction(joinPoint);

        //when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "loanId" });
        //authAspect.authorizeLoanAction(joinPoint);

        verify(joinPoint, times(2)).proceed();
    }

    @Test
    void authorizationTest_validTokenAccountAccess() throws Throwable {
        when(claims.get("id", Long.class)).thenReturn(1L);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Bearer ValidanToken", 1L });

        when(authService.parseToken(notNull(String.class))).thenReturn(claims);

        when(accountService.findById(1L)).thenReturn(account);
        //when(loanService.findById("1")).thenReturn(loan);
        when(cardService.findById(1L)).thenReturn(card);

        when(account.getOwnerID()).thenReturn(1L);
        //when(loan.getAccount()).thenReturn(account);
        when(card.getAccount()).thenReturn(account);

        when(accountAuthorization.customerOnlyOperation()).thenReturn(true);
        when(cardAuthorization.customerOnlyOperation()).thenReturn(true);
        //when(loanAuthorization.customerOnlyOperation()).thenReturn(true);

        when(method.getAnnotation(AccountAuthorization.class)).thenReturn(accountAuthorization);
        when(method.getAnnotation(CardAuthorization.class)).thenReturn(cardAuthorization);
        //when(method.getAnnotation(LoanAuthorization.class)).thenReturn(loanAuthorization);
        when(methodSignature.getParameterTypes()).thenReturn(new Class[] { String.class, Long.class });

        when(methodSignature.getMethod()).thenReturn(method);
        when(authService.getToken(notNull(String.class))).thenReturn("ValidanToken");

        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "accountId" });
        authAspect.authorizeAccountAction(joinPoint);

        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "cardId" });
        authAspect.authorizeCardAction(joinPoint);

        //when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "loanId" });
        //authAspect.authorizeLoanAction(joinPoint);

        verify(joinPoint, times(2)).proceed();
    }

    @Test
    void authorizationTest_validTokenMatchingId() throws Throwable {
        when(claims.get("id", Long.class)).thenReturn(1L);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "authorization", "userId" });
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Bearer ValidanToken", 1L });

        when(authService.parseToken(notNull(String.class))).thenReturn(claims);

        when(accountAuthorization.customerOnlyOperation()).thenReturn(true);

        when(method.getAnnotation(AccountAuthorization.class)).thenReturn(accountAuthorization);

        when(methodSignature.getMethod()).thenReturn(method);
        when(authService.getToken(notNull(String.class))).thenReturn("ValidanToken");

        authAspect.authorizeAccountAction(joinPoint);
        verify(joinPoint, times(1)).proceed();
    }
}
