package com.docplatform.master.controller;

import com.docplatform.master.entity.User;
import com.docplatform.master.service.UserService;
import com.docplatform.master.util.JwtUtil;
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
import java.util.HashMap;

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
            return new ResponseEntity<>(Map.of("accessToken", token, "username", registeredUser.getUsername()), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
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
            return new ResponseEntity<>(Map.of("accessToken", token, "username", username), HttpStatus.OK);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(Map.of("error", "Bad credentials"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("error", "Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/token/validate")
    public ResponseEntity<?> validateToken(Authentication authentication) {
        try {
            // 从 SecurityContext 中获取用户名
            String username = authentication.getName();
            
            // 这里可以返回 token 相关信息
            // 注意：实际的 token 有效期信息需要从请求头中解析
            // 为了简化，我们返回配置的过期时间
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("tokenExpiration", "24 hours"); // 24小时
            response.put("status", "valid");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", "Invalid token"), HttpStatus.UNAUTHORIZED);
        }
    }
    

}