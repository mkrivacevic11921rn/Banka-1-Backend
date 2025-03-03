package com.banka1.user.model.helper;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum Position {
    // primer pozicija koje zaposleni mogu da imaju u banci
    NONE,
    DIRECTOR,
    MANAGER,
    WORKER,
    HR
}
