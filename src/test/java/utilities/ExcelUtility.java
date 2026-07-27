package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtility {

    private String filePath;

    public ExcelUtility(String filePath) {
        this.filePath = filePath;
    }

    private XSSFWorkbook getWorkbook() throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        fis.close();
        return workbook;
    }

    public int getRowCount(String sheetName) throws IOException {
        XSSFWorkbook workbook = getWorkbook();
        XSSFSheet sheet = workbook.getSheet(sheetName);
        int rowCount = sheet.getLastRowNum();
        workbook.close();
        return rowCount;
    }

    public int getColCount(String sheetName) throws IOException {
        XSSFWorkbook workbook = getWorkbook();
        XSSFSheet sheet = workbook.getSheet(sheetName);
        int colCount = sheet.getRow(0).getLastCellNum();
        workbook.close();
        return colCount;
    }

    public String getCellData(String sheetName, int rowNum, int colNum) throws IOException {
        XSSFWorkbook workbook = getWorkbook();
        XSSFSheet sheet = workbook.getSheet(sheetName);
        Row row = sheet.getRow(rowNum);
        Cell cell = row.getCell(colNum);

        String data;
        switch (cell.getCellType()) {
            case STRING:
                data = cell.getStringCellValue();
                break;
            case NUMERIC:
                data = String.valueOf((long) cell.getNumericCellValue());
                break;
            case BOOLEAN:
                data = String.valueOf(cell.getBooleanCellValue());
                break;
            default:
                data = "";
        }
        workbook.close();
        return data;
    }

    public void setCellData(String sheetName, int rowNum, int colNum, String result) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        fis.close();

        XSSFSheet sheet = workbook.getSheet(sheetName);
        Row row = sheet.getRow(rowNum);
        Cell cell = row.createCell(colNum);
        cell.setCellValue(result);

        XSSFCellStyle style = workbook.createCellStyle();
        if (result.equalsIgnoreCase("PASS")) {
            style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        } else {
            style.setFillForegroundColor(IndexedColors.RED.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cell.setCellStyle(style);

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }
}