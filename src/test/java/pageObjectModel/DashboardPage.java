package pageObjectModel;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DashboardPage {

    WebDriver driver;
    WebDriverWait wait;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = ".oxd-topbar-header-breadcrumb h6")
    WebElement lblDashboardHeader;

    @FindBy(xpath = "//span[text()='Admin']")
    WebElement menuAdmin;

    @FindBy(xpath = "//span[text()='PIM']")
    WebElement menuPIM;

    @FindBy(xpath = "//span[text()='Leave']")
    WebElement menuLeave;

    @FindBy(css = ".oxd-userdropdown-tab")
    WebElement userDropdown;

    @FindBy(xpath = "//a[text()='Logout']")
    WebElement lnkLogout;

    public boolean isDashboardDisplayed() {
        wait.until(ExpectedConditions.visibilityOf(lblDashboardHeader));
        String actualText = lblDashboardHeader.getText();
        System.out.println("DEBUG - Dashboard header text found: [" + actualText + "]");
        return actualText.equalsIgnoreCase("Dashboard");
    }

    public AdminPage goToAdmin() {
        menuAdmin.click();
        return new AdminPage(driver);
    }

    public PIMPage goToPIM() {
        menuPIM.click();
        return new PIMPage(driver);
    }

    public LeavePage goToLeave() {
        menuLeave.click();
        return new LeavePage(driver);
    }

    public LoginPage logout() {
        userDropdown.click();
        wait.until(ExpectedConditions.elementToBeClickable(lnkLogout));
        lnkLogout.click();
        return new LoginPage(driver);
    }
}