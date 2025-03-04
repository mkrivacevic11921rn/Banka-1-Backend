package com.banka1.banking.aspect;

import com.banka1.banking.dto.response.AccountResponse;
import com.banka1.banking.dto.response.CardResponse;
import com.banka1.banking.dto.response.LoanResponse;
import com.banka1.banking.services.AccountService;
import com.banka1.banking.services.CardService;
import com.banka1.banking.services.LoanService;
import com.banka1.banking.services.implementation.AuthService;
import com.banka1.banking.utils.ResponseMessage;
import com.banka1.banking.utils.ResponseTemplate;
import com.banka1.banking.utils.ThrowingFunction;
import com.banka1.common.model.Position;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.IntStream;

@Aspect
@Configuration
@EnableAspectJAutoProxy
public class AuthAspect {
    private final AuthService authService;
    private final AccountService accountService;
    private final LoanService loanService;
    private final CardService cardService;

    public AuthAspect(AuthService authService, AccountService accountService, LoanService loanService, CardService cardService) {
        this.authService = authService;
        this.accountService = accountService;
        this.loanService = loanService;
        this.cardService = cardService;
    }

    // Izvlači token iz prosledjenog autorizacionog parametra
    private String parseAuthToken(String[] parameterNames, Object[] args) {
        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals("authorization") && args[i] != null) {
                return authService.getToken(args[i].toString());
            }
        }
        return null;
    }

    private String getAuthTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            return authService.getToken(authHeader);
        }
        return null;
    }



    private Object doAuth(MethodSignature methodSignature, ProceedingJoinPoint joinPoint, ThrowingFunction<Claims, Object> callback) throws Throwable {
        String token = parseAuthToken(methodSignature.getParameterNames(), joinPoint.getArgs());
        if(token == null)
            token = getAuthTokenFromRequest();

        if(token == null) {
            // Token ne postoji, tj. korisnik nije ulogovan
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.UNAUTHORIZED), false, null, ResponseMessage.INVALID_LOGIN.toString());
        }

        // Token je blacklist-ovan
        // Trenutno nemoguće proveriti

        Claims claims = authService.parseToken(token);
        if(claims == null) {
            // Token postoji ali nije autentičan, verovatno je pokušaj neodobrenog pristupa
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.UNAUTHORIZED), false, null, ResponseMessage.INVALID_LOGIN.toString());
        }

        return callback.apply(claims);
    }

    @Around("@annotation(com.banka1.banking.aspect.AccountAuthorization)")
    public Object authorizeAccountAction(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        return doAuth(methodSignature, joinPoint, (claims) -> {
            AccountAuthorization authorization = method.getAnnotation(AccountAuthorization.class);

            if(!authorization.customerOnlyOperation()) {
                if(!claims.get("position", String.class).equals(Position.NONE.toString())) {
                    // Zaposleni
                    return joinPoint.proceed();
                }
            }

            OptionalInt maybeUserIdIndex = IntStream.range(0, methodSignature.getParameterNames().length).filter(i -> methodSignature.getParameterNames()[i].compareTo("userId") == 0).findFirst();
            if(maybeUserIdIndex.isPresent()) {
                Long givenId = Long.parseLong(joinPoint.getArgs()[maybeUserIdIndex.getAsInt()].toString());
                if (Objects.equals(claims.get("id", Long.class), givenId))
                    return joinPoint.proceed();
            }

            OptionalInt maybeAccountIdIndex = IntStream.range(0, methodSignature.getParameterNames().length).filter(i -> methodSignature.getParameterNames()[i].compareTo("accountId") == 0).findFirst();
            if(maybeAccountIdIndex.isPresent()) {
                AccountResponse account = accountService.findById(joinPoint.getArgs()[maybeAccountIdIndex.getAsInt()].toString());
                if(account != null) {
                    if (Objects.equals(claims.get("id", Long.class), account.getOwnerID()))
                        return joinPoint.proceed();
                }
            }

            // ako je admin
            if(!authorization.disallowAdminFallback() && Objects.equals(claims.get("isAdmin", Boolean.class), true)) {
                return joinPoint.proceed();
            }

            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.FORBIDDEN), false, null, ResponseMessage.FORBIDDEN.toString());
        });
    }

    @Around("@annotation(com.banka1.banking.aspect.CardAuthorization)")
    public Object authorizeCardAction(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        return doAuth(methodSignature, joinPoint, (claims) -> {
            CardAuthorization authorization = method.getAnnotation(CardAuthorization.class);

            if(!authorization.customerOnlyOperation()) {
                if(!claims.get("position", String.class).equals(Position.NONE.toString())) {
                    // Zaposleni
                    return joinPoint.proceed();
                }
            }

            OptionalInt maybeAccountIdIndex = IntStream.range(0, methodSignature.getParameterNames().length).filter(i -> methodSignature.getParameterNames()[i].compareTo("accountId") == 0).findFirst();
            if(maybeAccountIdIndex.isPresent()) {
                AccountResponse account = accountService.findById(joinPoint.getArgs()[maybeAccountIdIndex.getAsInt()].toString());
                if(account != null) {
                    if (Objects.equals(claims.get("id", Long.class), account.getOwnerID()))
                        return joinPoint.proceed();
                }
            }

            OptionalInt maybeCardIdIndex = IntStream.range(0, methodSignature.getParameterNames().length).filter(i -> methodSignature.getParameterNames()[i].compareTo("cardId") == 0).findFirst();
            if(maybeCardIdIndex.isPresent()) {
                CardResponse card = cardService.findById(joinPoint.getArgs()[maybeCardIdIndex.getAsInt()].toString());
                if(card != null) {
                    AccountResponse account = card.getAccount();
                    if (Objects.equals(claims.get("id", Long.class), account.getOwnerID()))
                        return joinPoint.proceed();
                }
            }

            // ako je admin
            if(!authorization.disallowAdminFallback() && Objects.equals(claims.get("isAdmin", Boolean.class), true)) {
                return joinPoint.proceed();
            }

            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.FORBIDDEN), false, null, ResponseMessage.FORBIDDEN.toString());
        });
    }

    @Around("@annotation(com.banka1.banking.aspect.LoanAuthorization)")
    public Object authorizeLoanAction(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        return doAuth(methodSignature, joinPoint, (claims) -> {
            LoanAuthorization authorization = method.getAnnotation(LoanAuthorization.class);

            if(!authorization.customerOnlyOperation()) {
                if(!claims.get("position", String.class).equals(Position.NONE.toString())) {
                    // Zaposleni
                    return joinPoint.proceed();
                }
            }

            OptionalInt maybeAccountIdIndex = IntStream.range(0, methodSignature.getParameterNames().length).filter(i -> methodSignature.getParameterNames()[i].compareTo("accountId") == 0).findFirst();
            if(maybeAccountIdIndex.isPresent()) {
                AccountResponse account = accountService.findById(joinPoint.getArgs()[maybeAccountIdIndex.getAsInt()].toString());
                if(account != null) {
                    if (Objects.equals(claims.get("id", Long.class), account.getOwnerID()))
                        return joinPoint.proceed();
                }
            }

            OptionalInt maybeLoanIdIndex = IntStream.range(0, methodSignature.getParameterNames().length).filter(i -> methodSignature.getParameterNames()[i].compareTo("loanId") == 0).findFirst();
            if(maybeLoanIdIndex.isPresent()) {
                LoanResponse loan = loanService.findById(joinPoint.getArgs()[maybeLoanIdIndex.getAsInt()].toString());
                if(loan != null) {
                    if (Objects.equals(claims.get("id", Long.class), loan.getAccount().getOwnerID()))
                        return joinPoint.proceed();
                }
            }

            // ako je admin
            if(!authorization.disallowAdminFallback() && Objects.equals(claims.get("isAdmin", Boolean.class), true)) {
                return joinPoint.proceed();
            }

            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.FORBIDDEN), false, null, ResponseMessage.FORBIDDEN.toString());
        });
    }
}
