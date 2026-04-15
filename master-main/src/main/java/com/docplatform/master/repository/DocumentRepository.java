package com.docplatform.master.repository;

import com.docplatform.master.entity.Document;
import com.docplatform.master.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUser(User user);
    List<Document> findByUserAndConverted(User user, boolean converted);
}