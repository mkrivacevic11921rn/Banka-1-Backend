package com.banka1.user.service;

import io.jsonwebtoken.Claims;

/**
 * Servis za autentifikaciju i autorizaciju.
 * Odgovoran je za generisanje i validiranje JWT-ova i namenjen je za korišćenje od strane middleware-a koji se vezuje za kontroler metode drugih servisa.
 */
public interface IAuthService {
    /**
     * Parsira token za pristup.
     *
     * @param token String reprezentacija JWT-a koji se parsira.
     * @return {@link Claims} objekat koji sadrži informacije o korisniku koji je poslao request, ili <code>null</code> ako nije bilo moguće parsirati token.
     */
    Claims parseToken(String token);
}
