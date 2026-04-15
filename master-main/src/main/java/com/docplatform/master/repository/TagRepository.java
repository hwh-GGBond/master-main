package com.docplatform.master.repository;

import com.docplatform.master.entity.Tag;
import com.docplatform.master.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUser(User user);
    Optional<Tag> findByNameAndUser(String name, User user);
}