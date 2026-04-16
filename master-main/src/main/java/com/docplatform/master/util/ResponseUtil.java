package com.docplatform.master.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResponseUtil {
    public static ResponseEntity<?> success(Object data) {
        return success(data, HttpStatus.OK);
    }
    
    public static ResponseEntity<?> success(Object data, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("status", status.value());
        response.put("timestamp", System.currentTimeMillis());
        response.put("traceId", UUID.randomUUID().toString());
        
        return ResponseEntity.status(status)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(response);
    }
    
    public static ResponseEntity<?> error(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("status", status.value());
        response.put("timestamp", System.currentTimeMillis());
        response.put("traceId", UUID.randomUUID().toString());
        
        return ResponseEntity.status(status)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(response);
    }
    
    public static void error(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("traceId", UUID.randomUUID().toString());
        
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}