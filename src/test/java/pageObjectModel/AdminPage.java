package pageObjectModel;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AdminPage {

    WebDriver driver;
    WebDriverWait wait;

    public AdminPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = "h6.oxd-text--h6")
    WebElement lblAdminHeader;

    @FindBy(xpath = "//button[text()=' Add ']")
    WebElement btnAddUser;

    @FindBy(name = "username")
    WebElement txtUsername;

    @FindBy(css = ".oxd-table-body .oxd-table-row")
    List<WebElement> userRows;

    public boolean isAdminPageDisplayed() {
        wait.until(ExpectedConditions.visibilityOf(lblAdminHeader));
        return lblAdminHeader.getText().equalsIgnoreCase("Admin");
    }

    public void clickAddUser() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAddUser));
        btnAddUser.click();
    }

    public int getUserRowCount() {
        return userRows.size();
    }

    public void searchUserByUsername(String username) {
        txtUsername.clear();
        txtUsername.sendKeys(username);
    }
}