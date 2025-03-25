package com.banka1.banking.models.helper;

import java.util.ArrayList;
import java.util.List;

public enum BusinessActivityCode {

    COMPUTER_PROGRAMMING("6201", "Computer programming"),
    RETAIL_SALES("4711", "Retail sale in non-specialized stores with food, beverages, or tobacco predominating"),
    REAL_ESTATE_RENTAL("6820", "Renting and operating of own or leased real estate"),
    RESTAURANTS("5610", "Restaurants and mobile food service activities"),
    TAXI_SERVICE("4932", "Taxi operation");

    private final String code;
    private final String description;

    BusinessActivityCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static BusinessActivityCode fromCode(String code) {
        for (BusinessActivityCode activity : values()) {
            if (activity.getCode().equals(code)) {
                return activity;
            }
        }
        throw new IllegalArgumentException("Invalid activity code: " + code);
    }

    public static List<String> getAll() {
        List<String> codes = new ArrayList<>();
        for (BusinessActivityCode activity : values()) {
            codes.add(activity.getCode() + " -> " + activity.getDescription());
        }
        System.out.println(codes);
        return codes;
    }
}

