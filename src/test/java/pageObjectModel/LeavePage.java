package pageObjectModel;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LeavePage {

    WebDriver driver;
    WebDriverWait wait;
    Actions actions;

    public LeavePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.actions = new Actions(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//h6[text()='Leave']")
    WebElement lblLeaveHeader;

    @FindBy(xpath = "//a[text()='Apply']")
    WebElement lnkApply;

    @FindBy(css = ".oxd-select-text-input")
    List<WebElement> dropdowns;

    @FindBy(css = ".oxd-select-dropdown .oxd-select-option")
    List<WebElement> dropdownOptions;

    @FindBy(css = "textarea")
    WebElement txtComment;

    @FindBy(xpath = "//button[text()=' Apply ']")
    WebElement btnApplyLeave;

    public boolean isLeavePageDisplayed() {
        wait.until(ExpectedConditions.visibilityOf(lblLeaveHeader));
        return lblLeaveHeader.isDisplayed();
    }

    public void clickApply() {
        wait.until(ExpectedConditions.elementToBeClickable(lnkApply));
        lnkApply.click();
    }

    public void selectLeaveType(String leaveType) {
        wait.until(ExpectedConditions.visibilityOfAllElements(dropdowns));
        actions.moveToElement(dropdowns.get(0)).click().perform();
        wait.until(ExpectedConditions.visibilityOfAllElements(dropdownOptions));
        for (WebElement option : dropdownOptions) {
            if (option.getText().trim().equalsIgnoreCase(leaveType)) {
                option.click();
                break;
            }
        }
    }

    public void enterComment(String comment) {
        txtComment.clear();
        txtComment.sendKeys(comment);
    }

    public void submitLeaveApplication() {
        btnApplyLeave.click();
    }
}