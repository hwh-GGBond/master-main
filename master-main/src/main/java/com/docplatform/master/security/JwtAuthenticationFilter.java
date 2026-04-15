package com.docplatform.master.security;

import com.docplatform.master.util.JwtUtil;
import com.docplatform.master.util.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/auth/login");
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            Map<String, String> credentials = new ObjectMapper().readValue(request.getInputStream(), Map.class);
            String username = credentials.get("username");
            String password = credentials.get("password");
            
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            try {
                Map<String, Object> responseBody = new java.util.HashMap<>();
                responseBody.put("error", "Invalid request body format");
                responseBody.put("status", HttpServletResponse.SC_BAD_REQUEST);
                responseBody.put("timestamp", System.currentTimeMillis());
                responseBody.put("traceId", java.util.UUID.randomUUID().toString());
                response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
                response.getWriter().flush();
                response.getWriter().close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // 返回 null，不再抛出异常，避免过滤器链继续执行
            return null;
        }
    }
    
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String username = authResult.getName();
        String token = jwtUtil.generateToken(username);
        
        response.addHeader("Authorization", "Bearer " + token);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        
        Map<String, Object> responseBody = new java.util.HashMap<>();
        Map<String, String> data = new java.util.HashMap<>();
        data.put("accessToken", token);
        data.put("username", username);
        responseBody.put("data", data);
        responseBody.put("status", HttpServletResponse.SC_OK);
        responseBody.put("timestamp", System.currentTimeMillis());
        responseBody.put("traceId", java.util.UUID.randomUUID().toString());
        
        response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
    }
    
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        
        Map<String, Object> responseBody = new java.util.HashMap<>();
        responseBody.put("error", "Bad credentials");
        responseBody.put("status", HttpServletResponse.SC_BAD_REQUEST);
        responseBody.put("timestamp", System.currentTimeMillis());
        responseBody.put("traceId", java.util.UUID.randomUUID().toString());
        
        response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
    }
    
    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, jakarta.servlet.FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 检查是否是登录路径
        if (httpRequest.getRequestURI().equals("/api/auth/login")) {
            // 检查是否是 POST 方法
            if (!"POST".equals(httpRequest.getMethod())) {
                // 返回 405 错误
                httpResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                httpResponse.setContentType("application/json");
                Map<String, Object> responseBody = new java.util.HashMap<>();
                responseBody.put("error", "Method not allowed");
                responseBody.put("status", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                responseBody.put("timestamp", System.currentTimeMillis());
                responseBody.put("traceId", java.util.UUID.randomUUID().toString());
                response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
                response.getWriter().flush();
                response.getWriter().close();
                return;
            }
        }
        
        // 检查是否是登录请求
        if (requiresAuthentication(httpRequest, httpResponse)) {
            try {
                Authentication authResult = attemptAuthentication(httpRequest, httpResponse);
                // 如果 attemptAuthentication 返回 null，表示已经处理了错误并返回了响应
                if (authResult == null) {
                    return;
                }
                successfulAuthentication(httpRequest, httpResponse, chain, authResult);
            } catch (AuthenticationException failed) {
                unsuccessfulAuthentication(httpRequest, httpResponse, failed);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}