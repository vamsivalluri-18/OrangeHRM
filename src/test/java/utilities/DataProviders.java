package utilities;

import java.io.IOException;
import org.testng.annotations.DataProvider;

public class DataProviders {

    @DataProvider(name = "loginData")
    public Object[][] getLoginData() throws IOException {
        String path = "./testData/OrangeHRM_LoginData.xlsx";
        ExcelUtility excel = new ExcelUtility(path);
        int rowCount = excel.getRowCount("Sheet1");
        int colCount = excel.getColCount("Sheet1");
        Object[][] data = new Object[rowCount][colCount];
        for (int i = 1; i <= rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                data[i - 1][j] = excel.getCellData("Sheet1", i, j);
            }
        }
        return data;
    }

    @DataProvider(name = "employeeData")
    public Object[][] getEmployeeData() throws IOException {
        String path = "./testData/OrangeHRM_EmployeeData.xlsx";
        ExcelUtility excel = new ExcelUtility(path);
        int rowCount = excel.getRowCount("Sheet1");
        int colCount = excel.getColCount("Sheet1");
        Object[][] data = new Object[rowCount][colCount];
        for (int i = 1; i <= rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                data[i - 1][j] = excel.getCellData("Sheet1", i, j);
            }
        }
        return data;
    }
}