package com.docplatform.master.service;

import com.docplatform.master.entity.Document;
import com.docplatform.master.entity.User;
import com.docplatform.master.repository.DocumentRepository;
import com.docplatform.master.service.converter.ConverterFactory;
import com.docplatform.master.service.converter.DocumentConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class DocumentService {
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    public Document uploadDocument(MultipartFile file, User user) throws IOException {
        // Create upload directory if it doesn't exist
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Save file to disk
        Path filePath = Paths.get(uploadDir, uniqueFilename);
        Files.write(filePath, file.getBytes());
        
        // 检查是否存在同名文件
        Document existingDocument = documentRepository.findByUserAndOriginalName(user, originalFilename);
        Document savedDocument;
        
        if (existingDocument != null) {
            // 存在同名文件，执行更新操作
            // 保存旧文件路径，用于后续删除
            String oldFilePath = existingDocument.getFilePath();
            
            // 更新文档信息
            existingDocument.setTitle(originalFilename != null ? originalFilename.substring(0, originalFilename.lastIndexOf('.')) : "");
            existingDocument.setFilePath(filePath.toString());
            existingDocument.setFileSize(file.getSize());
            existingDocument.setFileType(file.getContentType());
            
            // 特殊处理 Markdown 文件
            if ("text/markdown".equals(file.getContentType())) {
                // 直接读取文件内容作为 mdContent
                String markdownContent = new String(Files.readAllBytes(filePath));
                existingDocument.setMdContent(markdownContent);
                existingDocument.setConverted(true);
            } else {
                existingDocument.setConverted(false);
                existingDocument.setMdContent(null);
            }
            
            savedDocument = documentRepository.save(existingDocument);
            
            // 清理旧文件
            try {
                Files.deleteIfExists(Paths.get(oldFilePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 不存在同名文件，执行新增操作
            Document document = new Document();
            document.setTitle(originalFilename != null ? originalFilename.substring(0, originalFilename.lastIndexOf('.')) : "");
            document.setOriginalName(originalFilename);
            document.setFilePath(filePath.toString());
            document.setFileSize(file.getSize());
            document.setFileType(file.getContentType());
            document.setUser(user);
            
            // 特殊处理 Markdown 文件
            if ("text/markdown".equals(file.getContentType())) {
                // 直接读取文件内容作为 mdContent
                String markdownContent = new String(Files.readAllBytes(filePath));
                document.setMdContent(markdownContent);
                document.setConverted(true);
            } else {
                document.setConverted(false);
            }
            
            savedDocument = documentRepository.save(document);
        }
        
        return savedDocument;
    }
    
    public Document convertDocument(Long id, User user) throws IOException {
        Document document = getDocumentById(id, user);
        
        // Check if document is already converted
        if (document.isConverted()) {
            return document;
        }
        
        String markdownContent;
        
        // 特殊处理 Markdown 文件
        if ("text/markdown".equals(document.getFileType())) {
            // 直接读取文件内容作为 mdContent
            File file = new File(document.getFilePath());
            markdownContent = new String(Files.readAllBytes(file.toPath()));
        } else {
            // Get appropriate converter
            DocumentConverter converter = ConverterFactory.getConverter(document.getFileType());
            if (converter == null) {
                throw new RuntimeException("Unsupported file type for conversion");
            }
            
            // Convert to markdown
            File file = new File(document.getFilePath());
            markdownContent = converter.convertToMarkdown(file);
        }
        
        // Update document
        document.setMdContent(markdownContent);
        document.setConverted(true);
        
        return documentRepository.save(document);
    }
    
    public List<Document> getDocumentsByUser(User user) {
        return documentRepository.findByUser(user);
    }
    
    public Map<String, Object> getDocumentsByUser(User user, int page, int size) {
        List<Document> documents = documentRepository.findByUser(user);
        
        // 计算总记录数
        int total = documents.size();
        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / size);
        // 计算起始索引
        int startIndex = page * size;
        // 计算结束索引
        int endIndex = Math.min(startIndex + size, total);
        
        // 截取分页数据
        List<Document> paginatedDocuments = new ArrayList<>();
        if (startIndex < total) {
            paginatedDocuments = documents.subList(startIndex, endIndex);
        }
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("documents", paginatedDocuments);
        result.put("total", total);
        result.put("totalPages", totalPages);
        result.put("page", page);
        result.put("size", size);
        
        return result;
    }
    
    public Document getDocumentById(Long id, User user) {
        Document document = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Document not found"));
        if (!document.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return document;
    }
    
    public void deleteDocument(Long id, User user) {
        Document document = getDocumentById(id, user);
        
        // Delete file from disk
        try {
            Files.deleteIfExists(Paths.get(document.getFilePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        documentRepository.delete(document);
    }
    
    public Document updateDocument(Long id, String title, String mdContent, User user) {
        Document document = getDocumentById(id, user);
        if (title != null) {
            document.setTitle(title);
        }
        if (mdContent != null) {
            document.setMdContent(mdContent);
            document.setConverted(true);
        }
        return documentRepository.save(document);
    }
}