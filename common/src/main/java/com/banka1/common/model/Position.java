package com.banka1.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    @JsonCreator
    public static Position fromString(String position) {
        for (Position p : Position.values()) {
            if (p.getPosition().equals(position)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Invalid position: " + position);
    }
}
