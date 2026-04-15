package com.docplatform.master.service;

import com.docplatform.master.entity.Document;
import com.docplatform.master.entity.Share;
import com.docplatform.master.entity.User;
import com.docplatform.master.repository.ShareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShareService {
    
    @Autowired
    private ShareRepository shareRepository;
    
    @Autowired
    private DocumentService documentService;
    
    public Share createShare(Long documentId, User user, String password, LocalDateTime expiresAt) {
        Document document = documentService.getDocumentById(documentId, user);
        
        // Generate unique share code
        String shareCode = UUID.randomUUID().toString().substring(0, 8);
        
        Share share = new Share();
        share.setDocument(document);
        share.setShareCode(shareCode);
        share.setPassword(password);
        share.setExpiresAt(expiresAt);
        
        return shareRepository.save(share);
    }
    
    public Document getSharedDocument(String shareCode, String password) {
        Share share = shareRepository.findByShareCode(shareCode).orElseThrow(() -> new RuntimeException("Share not found"));
        
        // Check if share has expired
        if (share.getExpiresAt() != null && share.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Share has expired");
        }
        
        // Check password if set
        if (share.getPassword() != null && !share.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        
        return share.getDocument();
    }
    
    public void deleteShare(Long shareId, User user) {
        Share share = shareRepository.findById(shareId).orElseThrow(() -> new RuntimeException("Share not found"));
        Document document = share.getDocument();
        
        // Check if user is the owner of the document
        if (!document.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        shareRepository.delete(share);
    }
}