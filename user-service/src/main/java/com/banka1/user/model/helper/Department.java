package com.banka1.user.model.helper;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Department {
    // primer odeljenja koje zaposleni mogu da imaju u banci
    ACCOUNTING("Računovodstvo"),
    FINANCIAL("Finansije"),
    CREDIT("Kredit"),
    LEGAL("Pravo"),
    IT("IT"),
    HR("HR");

    private final String department;

    Department(String department) {
        this.department = department;
    }

    public String getDepartement() {
        return department;
    }

    @JsonCreator
    public static Department fromString(String department) {
        for (Department d : Department.values()) {
            if (d.getDepartement().equals(department)) {
                return d;
            }
        }
        return null;
    }
}