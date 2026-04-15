package com.docplatform.master.service.converter;

import java.io.File;
import java.io.IOException;

public interface DocumentConverter {
    String convertToMarkdown(File file) throws IOException;
    boolean supports(String fileType);
}