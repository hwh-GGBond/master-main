package com.docplatform.master.service;

import com.docplatform.master.entity.Document;
import com.docplatform.master.entity.Tag;
import com.docplatform.master.entity.User;
import com.docplatform.master.repository.DocumentRepository;
import com.docplatform.master.service.TagService;
import com.docplatform.master.service.converter.ConverterFactory;
import com.docplatform.master.service.converter.DocumentConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class DocumentService {
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private TagService tagService;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Async
    public CompletableFuture<Document> uploadDocument(MultipartFile file, User user) throws IOException {
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
        
        // Create document entity
        Document document = new Document();
        document.setTitle(originalFilename != null ? originalFilename.substring(0, originalFilename.lastIndexOf('.')) : "");
        document.setOriginalName(originalFilename);
        document.setFilePath(filePath.toString());
        document.setFileSize(file.getSize());
        document.setFileType(file.getContentType());
        document.setConverted(false);
        document.setUser(user);
        
        Document savedDocument = documentRepository.save(document);
        return CompletableFuture.completedFuture(savedDocument);
    }
    
    @CachePut(value = "documents", key = "#id")
    public Document convertDocument(Long id, User user) throws IOException {
        Document document = getDocumentById(id, user);
        
        // Check if document is already converted
        if (document.isConverted()) {
            return document;
        }
        
        // Get appropriate converter
        DocumentConverter converter = ConverterFactory.getConverter(document.getFileType());
        if (converter == null) {
            throw new RuntimeException("Unsupported file type for conversion");
        }
        
        // Convert to markdown
        File file = new File(document.getFilePath());
        String markdownContent = converter.convertToMarkdown(file);
        
        // Update document
        document.setMdContent(markdownContent);
        document.setConverted(true);
        
        return documentRepository.save(document);
    }
    
    @Cacheable(value = "documents", key = "#user.id")
    public List<Document> getDocumentsByUser(User user) {
        return documentRepository.findByUser(user);
    }
    
    @Cacheable(value = "documents", key = "#id")
    public Document getDocumentById(Long id, User user) {
        Document document = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Document not found"));
        if (!document.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return document;
    }
    
    @CacheEvict(value = {"documents"}, key = "#id")
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
    
    @CachePut(value = "documents", key = "#id")
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
    
    @CachePut(value = "documents", key = "#documentId")
    public Document addTagToDocument(Long documentId, String tagName, User user) {
        Document document = getDocumentById(documentId, user);
        Tag tag = tagService.createTag(tagName, user);
        document.getTags().add(tag);
        return documentRepository.save(document);
    }
    
    @CachePut(value = "documents", key = "#documentId")
    public Document removeTagFromDocument(Long documentId, Long tagId, User user) {
        Document document = getDocumentById(documentId, user);
        Tag tag = tagService.getTagById(tagId, user);
        document.getTags().remove(tag);
        return documentRepository.save(document);
    }
    
    public List<Document> searchDocumentsByTag(String tagName, User user) {
        Tag tag = tagService.getTagByName(tagName, user);
        return new ArrayList<>(tag.getDocuments());
    }
}