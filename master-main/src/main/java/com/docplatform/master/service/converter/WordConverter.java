package com.docplatform.master.service.converter;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WordConverter implements DocumentConverter {
    @Override
    public String convertToMarkdown(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            StringBuilder markdown = new StringBuilder();
            markdown.append("# Word Document\n\n");
            
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (!text.isEmpty()) {
                    // Check if it's a heading
                    if (paragraph.getStyle() != null && paragraph.getStyle().startsWith("Heading")) {
                        int level = Integer.parseInt(paragraph.getStyle().substring(7));
                        for (int i = 0; i < level; i++) {
                            markdown.append("#");
                        }
                        markdown.append(" " + text + "\n\n");
                    } else {
                        markdown.append(text + "\n\n");
                    }
                }
            }
            
            return markdown.toString();
        }
    }
    
    @Override
    public boolean supports(String fileType) {
        return fileType != null && (fileType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || fileType.endsWith(".docx"));
    }
}