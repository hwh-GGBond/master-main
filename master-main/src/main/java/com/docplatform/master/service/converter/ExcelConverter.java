package com.docplatform.master.service.converter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ExcelConverter implements DocumentConverter {
    @Override
    public String convertToMarkdown(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            
            StringBuilder markdown = new StringBuilder();
            markdown.append("# Excel Document\n\n");
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                markdown.append("## Sheet: " + sheet.getSheetName() + "\n\n");
                
                // Create table header
                markdown.append("|");
                Row headerRow = sheet.getRow(0);
                if (headerRow != null) {
                    for (Cell cell : headerRow) {
                        markdown.append(" " + getCellValue(cell) + " |");
                    }
                    markdown.append("\n|");
                    for (Cell cell : headerRow) {
                        markdown.append(" --- |");
                    }
                    markdown.append("\n");
                    
                    // Create table rows
                    for (int j = 1; j <= sheet.getLastRowNum(); j++) {
                        Row row = sheet.getRow(j);
                        if (row != null) {
                            markdown.append("|");
                            for (Cell cell : row) {
                                markdown.append(" " + getCellValue(cell) + " |");
                            }
                            markdown.append("\n");
                        }
                    }
                }
                markdown.append("\n");
            }
            
            return markdown.toString();
        }
    }
    
    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    
    @Override
    public boolean supports(String fileType) {
        return fileType != null && (fileType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") || fileType.endsWith(".xlsx"));
    }
}