package com.docplatform.master.controller;

import com.docplatform.master.entity.Document;
import com.docplatform.master.entity.User;
import com.docplatform.master.service.DocumentService;
import com.docplatform.master.service.UserService;
import com.docplatform.master.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    
    @Autowired
    private DocumentService documentService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<?>> uploadDocument(@RequestParam("file") MultipartFile file, Authentication authentication) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
                try {
                    Document document = documentService.uploadDocument(file, user).join();
                    return ResponseUtil.success(document, HttpStatus.CREATED);
                } catch (IOException e) {
                    return ResponseUtil.error("Failed to upload document", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } catch (RuntimeException e) {
                return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        });
    }
    
    @PostMapping("/{id}/convert")
    public ResponseEntity<?> convertDocument(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            Document document = documentService.convertDocument(id, user);
            return ResponseUtil.success(document);
        } catch (IOException e) {
            return ResponseUtil.error("Failed to convert document", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getDocuments(Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            List<Document> documents = documentService.getDocumentsByUser(user);
            return ResponseUtil.success(documents);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            Document document = documentService.getDocumentById(id, user);
            return ResponseUtil.success(document);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            Document document = documentService.getDocumentById(id, user);
            
            Resource resource = new FileSystemResource(document.getFilePath());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document.getOriginalName())
                    .body(resource);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            documentService.deleteDocument(id, user);
            Map<String, String> responseData = new HashMap<>();
            responseData.put("message", "Document deleted successfully");
            return ResponseUtil.success(responseData);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocument(@PathVariable Long id, @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            String title = request.get("title");
            String mdContent = request.get("mdContent");
            Document document = documentService.updateDocument(id, title, mdContent, user);
            return ResponseUtil.success(document);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}