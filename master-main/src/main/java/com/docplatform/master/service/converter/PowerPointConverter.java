package com.docplatform.master.service.converter;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PowerPointConverter implements DocumentConverter {
    @Override
    public String convertToMarkdown(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {
            
            StringBuilder markdown = new StringBuilder();
            markdown.append("# PowerPoint Document\n\n");
            
            int slideNumber = 1;
            for (XSLFSlide slide : ppt.getSlides()) {
                markdown.append("## Slide " + slideNumber + "\n\n");
                
                for (Object shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        String text = textShape.getText();
                        if (!text.isEmpty()) {
                            markdown.append(text + "\n\n");
                        }
                    }
                }
                
                slideNumber++;
            }
            
            return markdown.toString();
        }
    }
    
    @Override
    public boolean supports(String fileType) {
        return fileType != null && (fileType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") || fileType.endsWith(".pptx"));
    }
}
