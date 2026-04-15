package com.docplatform.master.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResponseUtil {

    public static ResponseEntity<?> error(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("status", status.value());
        response.put("timestamp", System.currentTimeMillis());
        response.put("traceId", UUID.randomUUID().toString());
        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<?> success(Object data, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("status", status.value());
        response.put("timestamp", System.currentTimeMillis());
        response.put("traceId", UUID.randomUUID().toString());
        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<?> success(Object data) {
        return success(data, HttpStatus.OK);
    }

    public static ResponseEntity<?> error(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }

    public static ResponseEntity<?> error(String message, int statusCode) {
        return error(message, HttpStatus.valueOf(statusCode));
    }
}