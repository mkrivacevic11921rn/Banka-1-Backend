package com.banka1.user.utils;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public final class ResponseTemplate {

    private static final String UNKNOWN_ERROR = "Nepoznata greška";

    private ResponseTemplate(){

    }

    public static ResponseEntity<?> create(ResponseEntity.BodyBuilder builder, boolean success, Map<?,?> data,String error){
        if (success){
            return builder.body(Map.of(
                    "success" , true,
                    "data" , data == null ? Map.of() : data
            ));
        }else {
            return builder.body(Map.of(
                    "success" , false,
                    "error" , error == null ? UNKNOWN_ERROR : error
            ));
        }
    }

    public static ResponseEntity<?> create(ResponseEntity.BodyBuilder builder,Exception e){
        return create(builder,false,null,e.getMessage());
    }

}
