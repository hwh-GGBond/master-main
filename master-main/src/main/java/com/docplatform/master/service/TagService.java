package com.docplatform.master.service;

import com.docplatform.master.entity.Tag;
import com.docplatform.master.entity.User;
import com.docplatform.master.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {
    
    @Autowired
    private TagRepository tagRepository;
    
    public Tag createTag(String name, User user) {
        Tag tag = tagRepository.findByNameAndUser(name, user).orElse(null);
        if (tag != null) {
            return tag;
        }
        tag = new Tag();
        tag.setName(name);
        tag.setUser(user);
        return tagRepository.save(tag);
    }
    
    public List<Tag> getTagsByUser(User user) {
        return tagRepository.findByUser(user);
    }
    
    public Tag getTagById(Long id, User user) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new RuntimeException("Tag not found"));
        if (!tag.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return tag;
    }
    
    public void deleteTag(Long id, User user) {
        Tag tag = getTagById(id, user);
        tagRepository.delete(tag);
    }
    
    public Tag getTagByName(String name, User user) {
        return tagRepository.findByNameAndUser(name, user).orElseThrow(() -> new RuntimeException("Tag not found"));
    }
}