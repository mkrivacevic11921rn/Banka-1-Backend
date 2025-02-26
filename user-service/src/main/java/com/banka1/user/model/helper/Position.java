package com.banka1.user.model.helper;

import lombok.Getter;

@Getter
public enum Position {
    // primer pozicija koje zaposleni mogu da imaju u banci
    NONE("Nijedna"),
    DIRECTOR("Direktor"),
    MANAGER("Menadžer"),
    WORKER("Radnik"),
    HR("HR");

    private final String position;

    Position(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return getPosition();
    }
}
