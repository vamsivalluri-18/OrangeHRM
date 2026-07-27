package testCases;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import basePage.BaseClass;
import pageObjectModel.DashboardPage;
import pageObjectModel.LoginPage;
import utilities.DataProviders;
import utilities.ExcelUtility;

public class LoginTest extends BaseClass {

    @Test(groups = { "Sanity", "Master" }, priority = 1)
    public void validLogin() {
        LoginPage login = new LoginPage(BaseClass.getDriver());
        DashboardPage dashboard = login.login(p.getProperty("username"), p.getProperty("password"));

        Assert.assertTrue(dashboard.isDashboardDisplayed(), "Dashboard was not displayed after valid login");
        logger.info("Valid login test passed");
    }

    @Test(groups = { "Regression", "Master" }, priority = 2)
    public void invalidLogin() {
        LoginPage login = new LoginPage(BaseClass.getDriver());
        login.enterUsername("InvalidUser");
        login.enterPassword("wrongPassword");
        login.clickLogin();

        String actualError = login.getErrorMessage();
        Assert.assertEquals(actualError, "Invalid credentials", "Error message did not match for invalid login");
        logger.info("Invalid login test passed");
    }

    @Test(groups = { "Regression" }, priority = 3)
    public void loginWithMultipleUsers() throws IOException {
        ExcelUtility excel = new ExcelUtility("./testData/OrangeHRM_LoginData.xlsx");
        int rowCount = excel.getRowCount("Sheet1");

        LoginPage login = new LoginPage(BaseClass.getDriver());

        for (int i = 1; i <= rowCount; i++) {
            String username = excel.getCellData("Sheet1", i, 0);
            String password = excel.getCellData("Sheet1", i, 1);
            String expectedResult = excel.getCellData("Sheet1", i, 2);

            login.enterUsername(username);
            login.enterPassword(password);
            login.clickLogin();

            if (expectedResult.equalsIgnoreCase("valid")) {
                DashboardPage dashboard = new DashboardPage(BaseClass.getDriver());
                Assert.assertTrue(dashboard.isDashboardDisplayed(),
                        "Expected dashboard after valid login for user: " + username);
                login = dashboard.logout();  // logout() already returns a LoginPage - reuse it for the next row
                logger.info("Valid login passed for: " + username);
            } else {
                Assert.assertTrue(login.getErrorMessage().length() > 0,
                        "Expected an error message for invalid login: " + username);
                logger.info("Invalid login correctly rejected for: " + username);
                // still on the login page after a failed attempt - ready for the next row as-is
            }
        }
    }
}