package com.banka1.user.service;

import io.jsonwebtoken.Claims;

public interface IAuthService {
    Claims parseToken(String token);
}
