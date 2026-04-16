package com.docplatform.master.service.converter;

import java.io.File;
import java.io.IOException;

public class ImageConverter implements DocumentConverter {
    @Override
    public String convertToMarkdown(File file) throws IOException {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Image Document\n\n");
        markdown.append("![Image]()\n\n");
        markdown.append("*Image: " + file.getName() + "*\n");
        
        return markdown.toString();
    }
    
    @Override
    public boolean supports(String fileType) {
        return fileType != null && (
            fileType.startsWith("image/") ||
            fileType.endsWith(".jpg") ||
            fileType.endsWith(".jpeg") ||
            fileType.endsWith(".png") ||
            fileType.endsWith(".gif") ||
            fileType.endsWith(".bmp") ||
            fileType.endsWith(".webp") ||
            fileType.endsWith(".svg")
        );
    }
}
