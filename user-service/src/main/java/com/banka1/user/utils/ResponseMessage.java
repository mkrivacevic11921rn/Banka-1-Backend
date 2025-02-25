package com.banka1.user.utils;

import lombok.Getter;

@Getter
public enum ResponseMessage {
    INVALID_LOGIN("Token nije prosleđen ili je neispravan/istekao."),
    FORBIDDEN("Nedovoljna autorizacija."),
    INVALID_USER("Korisnik ne postoji."),
    LOGOUT_SUCCESS("Korisnik odjavljen.");
    private final String message;

    ResponseMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
