package com.docplatform.master.service.converter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PdfConverter implements DocumentConverter {
    @Override
    public String convertToMarkdown(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            String text = textStripper.getText(document);
            
            // Basic markdown formatting
            StringBuilder markdown = new StringBuilder();
            markdown.append("# PDF Document\n\n");
            markdown.append(text);
            
            return markdown.toString();
        }
    }
    
    @Override
    public boolean supports(String fileType) {
        return fileType != null && (fileType.equals("application/pdf") || fileType.endsWith(".pdf"));
    }
}