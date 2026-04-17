package com.docplatform.master.service.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class CodeConverter implements DocumentConverter {
    @Override
    public String convertToMarkdown(File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        
        // 获取文件扩展名作为代码语言
        String fileName = file.getName();
        String language = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            language = fileName.substring(dotIndex + 1);
        }
        
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Code Document\n\n");
        markdown.append("```" + language + "\n");
        markdown.append(content);
        markdown.append("\n```");
        
        return markdown.toString();
    }
    
    @Override
    public boolean supports(String fileType) {
        return fileType != null && (
            fileType.startsWith("text/") || 
            fileType.endsWith(".java") ||
            fileType.endsWith(".js") ||
            fileType.endsWith(".html") ||
            fileType.endsWith(".css") ||
            fileType.endsWith(".json") ||
            fileType.endsWith(".xml") ||
            fileType.endsWith(".yaml") ||
            fileType.endsWith(".yml") ||
            fileType.endsWith(".py") ||
            fileType.endsWith(".go") ||
            fileType.endsWith(".c") ||
            fileType.endsWith(".cpp") ||
            fileType.endsWith(".cs") ||
            fileType.endsWith(".php") ||
            fileType.endsWith(".rb") ||
            fileType.endsWith(".swift") ||
            fileType.endsWith(".kt") ||
            fileType.endsWith(".ts") ||
            fileType.endsWith(".sh") ||
            fileType.endsWith(".md")
        );
    }
}
