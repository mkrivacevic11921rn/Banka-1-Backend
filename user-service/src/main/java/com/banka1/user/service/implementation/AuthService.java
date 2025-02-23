package com.banka1.user.service.implementation;

import com.banka1.user.service.IAuthService;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * Standardna implementacija {@link IAuthService} interfejsa.
 */
@Service
public class AuthService implements IAuthService {
    @Value("${oauth.jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    @Override
    public Claims parseToken(String token) {
        try {
            return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
