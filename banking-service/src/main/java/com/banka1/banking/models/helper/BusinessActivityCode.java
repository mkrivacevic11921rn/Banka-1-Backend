package com.banka1.banking.models.helper;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
@Getter
public enum BusinessActivityCode {

    COMPUTER_PROGRAMMING(6201.0, "Computer programming"),
    RETAIL_SALES(4711.0, "Retail sale in non-specialized stores with food, beverages, or tobacco predominating"),
    REAL_ESTATE_RENTAL(6820.0, "Renting and operating of own or leased real estate"),
    RESTAURANTS(5610.0, "Restaurants and mobile food service activities"),
    TAXI_SERVICE(4932.0, "Taxi operation"),
    BANK(1111.0, "Bank"),
    COUNTRY(2222.0, "Country");

    private final Double code;
    private final String description;

    BusinessActivityCode(Double code, String description) {
        this.code = code;
        this.description = description;
    }

    public static BusinessActivityCode fromCode(Double code) {
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
            codes.add(String.format("%.0f -> %s", activity.getCode(), activity.getDescription()));
        }
        System.out.println(codes);
        return codes;
    }
}

