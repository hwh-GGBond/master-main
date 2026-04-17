package com.docplatform.master.controller;

import com.docplatform.master.entity.User;
import com.docplatform.master.service.UserService;
import com.docplatform.master.util.JwtUtil;
import com.docplatform.master.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            String token = jwtUtil.generateToken(registeredUser.getUsername());
            Map<String, Object> response = Map.of("accessToken", token, "username", registeredUser.getUsername());
            return ResponseUtil.success(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            String token = jwtUtil.generateToken(username);
            Map<String, Object> response = Map.of("accessToken", token, "username", username);
            return ResponseUtil.success(response);
        } catch (AuthenticationException e) {
            return ResponseUtil.error("Bad credentials", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.error("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/token/expiration")
    public ResponseEntity<?> getTokenExpiration(Authentication authentication) {
        try {
            // 从 SecurityContext 中获取用户名
            String username = authentication.getName();
            
            // 返回token有效期信息
            Map<String, Object> response = Map.of(
                "username", username,
                "tokenExpiration", "24 hours", // 24小时
                "status", "valid"
            );
            
            return ResponseUtil.success(response);
        } catch (Exception e) {
            return ResponseUtil.error("Invalid token", HttpStatus.UNAUTHORIZED);
        }
    }
    

}