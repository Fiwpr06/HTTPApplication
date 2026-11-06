package com.fiwpr06.httpapplication.common.constant;

public enum HttpMethod {
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    OPTIONS;

    public static HttpMethod fromString(String method) {
        if (method == null) return GET;
        try {
            return HttpMethod.valueOf(method.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return GET;
        }
    }
}
