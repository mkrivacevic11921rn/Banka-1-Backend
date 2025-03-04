package com.banka1.banking.utils;

import lombok.Getter;

@Getter
public enum ResponseMessage {
    INVALID_LOGIN("Token nije prosleđen ili je neispravan/istekao."),
    FORBIDDEN("Nedovoljna autorizacija."),
    INVALID_USER("Korisnik ne postoji."),
    PASSWORD_RESET_REQUEST_SUCCESS("Zahtev za resetovanje lozinke uspešno poslat."),
    PASSWORD_RESET_SUCCESS("Lozinka uspešno resetovana."),
    LOGOUT_SUCCESS("Korisnik odjavljen."),
    CARD_NOT_FOUND("Nema kartica za traženi račun."),
    CARD_CREATED_SUCCESS("Kartica uspešno kreirana."),
    CARD_UPDATED_SUCCESS("Kartica uspešno ažurirana."),
    INVALID_REQUEST("Nevalidni podaci.");
    private final String message;

    ResponseMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
