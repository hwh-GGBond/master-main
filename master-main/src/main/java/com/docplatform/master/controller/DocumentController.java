package com.docplatform.master.controller;

import com.docplatform.master.entity.Document;
import com.docplatform.master.entity.User;
import com.docplatform.master.exception.InvalidPageParamsException;
import com.docplatform.master.exception.PageOutOfRangeException;
import com.docplatform.master.exception.UserNotFoundException;
import com.docplatform.master.service.DocumentService;
import com.docplatform.master.service.UserService;
import com.docplatform.master.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new UserNotFoundException("User not found"));
            try {
                Document document = documentService.uploadDocument(file, user);
                return ResponseUtil.success(document, HttpStatus.CREATED);
            } catch (IOException e) {
                return ResponseUtil.error("Failed to upload document", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (UserNotFoundException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getDocuments(Authentication authentication, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new UserNotFoundException("User not found"));
            Map<String, Object> result = documentService.getDocumentsByUser(user, page, size);
            return ResponseUtil.success(result);
        } catch (UserNotFoundException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (PageOutOfRangeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (InvalidPageParamsException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new UserNotFoundException("User not found"));
            Document document = documentService.getDocumentById(id, user);
            
            // 如果文档还没有转换为 Markdown，自动进行转换
            if (!document.isConverted()) {
                document = documentService.convertDocument(id, user);
            }
            
            return ResponseUtil.success(document);
        } catch (IOException e) {
            return ResponseUtil.error("Failed to convert document", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new UserNotFoundException("User not found"));
            Document document = documentService.getDocumentById(id, user);
            
            Resource resource;
            String fileType = document.getFileType();
            if (fileType.startsWith("text/") || fileType.equals("application/json") || fileType.equals("application/javascript")) {
                fileType = fileType + "; charset=UTF-8";
            }
            
            String encodedFilename = URLEncoder.encode(document.getOriginalName(), StandardCharsets.UTF_8);
            
            // 如果文档已转换为 Markdown，返回转换后的内容
            if (document.isConverted() && document.getMdContent() != null) {
                byte[] content = document.getMdContent().getBytes(StandardCharsets.UTF_8);
                resource = new ByteArrayResource(content) {
                    @Override
                    public String getFilename() {
                        return encodedFilename;
                    }
                };
            } else {
                // 否则返回原始文件
                resource = new FileSystemResource(document.getFilePath()) {
                    @Override
                    public String getFilename() {
                        return encodedFilename;
                    }
                };
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new UserNotFoundException("User not found"));
            documentService.deleteDocument(id, user);
            Map<String, String> responseData = new HashMap<>();
            responseData.put("message", "Document deleted successfully");
            return ResponseUtil.success(responseData);
        } catch (UserNotFoundException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocument(@PathVariable Long id, @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new UserNotFoundException("User not found"));
            String title = request.get("title");
            String mdContent = request.get("mdContent");
            Document document = documentService.updateDocument(id, title, mdContent, user);
            return ResponseUtil.success(document);
        } catch (UserNotFoundException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}