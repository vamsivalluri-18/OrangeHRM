package testCases;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import basePage.BaseClass;
import pageObjectModel.DashboardPage;
import pageObjectModel.LoginPage;
import pageObjectModel.PIMPage;
import utilities.ExcelUtility;

public class PIMTest extends BaseClass {

    @Test(groups = { "Sanity", "Master" }, priority = 1)
    public void addNewEmployee() {
        LoginPage loginPage = new LoginPage(BaseClass.getDriver());
        DashboardPage dashboard = loginPage.login(p.getProperty("username"), p.getProperty("password"));
        Assert.assertTrue(dashboard.isDashboardDisplayed());

        PIMPage pim = dashboard.goToPIM();
        Assert.assertTrue(pim.isPIMPageDisplayed());

        pim.clickAddEmployee();
        pim.enterFirstName("Ravi");
        pim.enterLastName("Kumar");
        pim.saveEmployee();

        logger.info("New employee added successfully");
    }

    @Test(groups = { "Regression", "Master" }, priority = 2)
    public void addMultipleEmployees() throws IOException {
        LoginPage loginPage = new LoginPage(BaseClass.getDriver());
        DashboardPage dashboard = loginPage.login(p.getProperty("username"), p.getProperty("password"));

        // Read employee data directly instead of using @DataProvider,
        // so this stays a single test method / single login session
        ExcelUtility excel = new ExcelUtility("./testData/OrangeHRM_EmployeeData.xlsx");
        int rowCount = excel.getRowCount("Sheet1");

        for (int i = 1; i <= rowCount; i++) {
            String firstName = excel.getCellData("Sheet1", i, 0);
            String lastName = excel.getCellData("Sheet1", i, 1);

            PIMPage pim = dashboard.goToPIM();
            pim.clickAddEmployee();
            pim.enterFirstName(firstName);
            pim.enterLastName(lastName);
            pim.saveEmployee();

            // re-fetch DashboardPage since the top nav can re-render after save
            dashboard = new DashboardPage(BaseClass.getDriver());
            pim = dashboard.goToPIM();

            pim.searchEmployeeByName(firstName + " " + lastName);
            Assert.assertTrue(pim.isEmployeeListedInTable(firstName + " " + lastName),
                    "Employee not found in the list after creation: " + firstName + " " + lastName);

            logger.info("Added and verified employee: " + firstName + " " + lastName);
        }

        dashboard.logout();
    }
}