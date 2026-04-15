package com.docplatform.master.security;

import com.docplatform.master.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private final JwtUtil jwtUtil;
    
    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        
        String token = header.replace("Bearer ", "");
        
        try {
            String username = jwtUtil.extractUsername(token);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.validateToken(token, username)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Token 过期或无效
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    java.util.Map<String, Object> responseBody = new java.util.HashMap<>();
                    responseBody.put("error", "Token expired or invalid");
                    responseBody.put("status", HttpServletResponse.SC_UNAUTHORIZED);
                    responseBody.put("timestamp", System.currentTimeMillis());
                    responseBody.put("traceId", java.util.UUID.randomUUID().toString());
                    response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(responseBody));
                    response.getWriter().flush();
                    response.getWriter().close();
                    return;
                }
            }
        } catch (Exception e) {
            // Token 格式错误或其他异常
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            java.util.Map<String, Object> responseBody = new java.util.HashMap<>();
            responseBody.put("error", "Invalid token format");
            responseBody.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            responseBody.put("timestamp", System.currentTimeMillis());
            responseBody.put("traceId", java.util.UUID.randomUUID().toString());
            response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(responseBody));
            response.getWriter().flush();
            response.getWriter().close();
            return;
        }
        
        chain.doFilter(request, response);
    }
}