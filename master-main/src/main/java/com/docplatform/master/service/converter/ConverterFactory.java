package com.docplatform.master.service.converter;

import java.util.ArrayList;
import java.util.List;

public class ConverterFactory {
    private static final List<DocumentConverter> converters = new ArrayList<>();
    
    static {
        converters.add(new PdfConverter());
        converters.add(new WordConverter());
        converters.add(new ExcelConverter());
    }
    
    public static DocumentConverter getConverter(String fileType) {
        for (DocumentConverter converter : converters) {
            if (converter.supports(fileType)) {
                return converter;
            }
        }
        return null;
    }
}