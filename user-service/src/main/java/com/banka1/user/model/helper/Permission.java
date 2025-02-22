package com.banka1.user.model.helper;

import lombok.Getter;

@Getter
public enum Permission {
    CREATE_EMPLOYEE("user.employee.create"),
    READ_EMPLOYEE("user.employee.view");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }
}
