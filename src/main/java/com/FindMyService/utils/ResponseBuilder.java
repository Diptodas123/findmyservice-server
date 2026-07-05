package com.FindMyService.utils;

import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ResponseBuilder {

    private ResponseBuilder() {}

    public static Map<String, Object> build(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }

}
