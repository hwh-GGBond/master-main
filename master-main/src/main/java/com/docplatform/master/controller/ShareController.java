package com.docplatform.master.controller;

import com.docplatform.master.entity.Document;
import com.docplatform.master.entity.Share;
import com.docplatform.master.entity.User;
import com.docplatform.master.service.ShareService;
import com.docplatform.master.service.UserService;
import com.docplatform.master.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/shares")
public class ShareController {
    
    @Autowired
    private ShareService shareService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<?> createShare(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            Long documentId = Long.valueOf(request.get("documentId").toString());
            String password = (String) request.get("password");
            String expiresAtStr = (String) request.get("expiresAt");
            
            LocalDateTime expiresAt = null;
            if (expiresAtStr != null && !expiresAtStr.isEmpty()) {
                expiresAt = LocalDateTime.parse(expiresAtStr);
            }
            
            Share share = shareService.createShare(documentId, user, password, expiresAt);
            return ResponseUtil.success(share);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/{shareCode}")
    public ResponseEntity<?> getSharedDocument(@PathVariable String shareCode, @RequestParam(required = false) String password) {
        try {
            Document document = shareService.getSharedDocument(shareCode, password);
            return ResponseUtil.success(document);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShare(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            shareService.deleteShare(id, user);
            Map<String, String> responseData = Map.of("message", "Share deleted successfully");
            return ResponseUtil.success(responseData);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}