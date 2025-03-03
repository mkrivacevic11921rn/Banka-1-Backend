package com.banka1.user.model.helper;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Department {
    // primer odeljenja koje zaposleni mogu da imaju u banci
    ACCOUNTING,
    FINANCIAL,
    CREDIT,
    LEGAL,
    IT,
    HR
}