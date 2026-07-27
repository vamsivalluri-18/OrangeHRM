package pageObjectModel;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PIMPage {

    WebDriver driver;
    WebDriverWait wait;

    public PIMPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//h6[text()='PIM']")
    WebElement lblPIMHeader;

    @FindBy(xpath = "//button[text()=' Add ']")
    WebElement btnAddEmployee;

    @FindBy(name = "firstName")
    WebElement txtFirstName;

    @FindBy(name = "lastName")
    WebElement txtLastName;

    @FindBy(css = "input[placeholder='Employee Id']")
    WebElement txtEmployeeId;

    @FindBy(xpath = "//button[@type='submit' and contains(.,'Save')]")
    WebElement btnSaveEmployee;

    @FindBy(css = "input[placeholder='Type for hints...']")
    WebElement txtEmployeeSearchName;

    @FindBy(xpath = "//button[@type='submit' and contains(.,'Search')]")
    WebElement btnSearch;

    public boolean isPIMPageDisplayed() {
        wait.until(ExpectedConditions.visibilityOf(lblPIMHeader));
        return lblPIMHeader.isDisplayed();
    }

    public void clickAddEmployee() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAddEmployee));
        btnAddEmployee.click();
    }

    public void enterFirstName(String firstName) {
        wait.until(ExpectedConditions.visibilityOf(txtFirstName));
        txtFirstName.clear();
        txtFirstName.sendKeys(firstName);
    }

    public void enterLastName(String lastName) {
        txtLastName.clear();
        txtLastName.sendKeys(lastName);
    }

    public String getGeneratedEmployeeId() {
        return txtEmployeeId.getAttribute("value");
    }

    public void saveEmployee() {
        wait.until(ExpectedConditions.elementToBeClickable(btnSaveEmployee));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnSaveEmployee);
        try {
            btnSaveEmployee.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnSaveEmployee);
        }
        waitForSpinnerToDisappear();
    }

    public void searchEmployeeByName(String name) {
        wait.until(ExpectedConditions.visibilityOf(txtEmployeeSearchName));
        txtEmployeeSearchName.clear();
        txtEmployeeSearchName.sendKeys(name);

        wait.until(ExpectedConditions.elementToBeClickable(btnSearch));
        btnSearch.click();

        waitForSpinnerToDisappear();

        // Wait for the table to genuinely refresh, retrying on staleness
        // since Angular can re-render mid-poll
        withStaleRetry(() -> {
            wait.until(d -> {
                List<WebElement> rows = d.findElements(By.cssSelector(".oxd-table-body .oxd-table-row"));
                return !rows.isEmpty();
            });
            return null;
        });
    }

    public boolean isEmployeeListedInTable(String fullName) {
        String normalizedTarget = fullName.trim().replaceAll("\\s+", " ");

        return withStaleRetry(() -> {
            List<WebElement> freshRows = wait.until(d -> {
                List<WebElement> rows = d.findElements(By.cssSelector(".oxd-table-body .oxd-table-row"));
                return rows.isEmpty() ? null : rows;
            });

            for (WebElement row : freshRows) {
                String normalizedRow = row.getText().trim().replaceAll("\\s+", " ");
                if (normalizedRow.contains(normalizedTarget)) {
                    return true;
                }
            }
            return false;
        });
    }

    // Waits for OrangeHRM's loading spinner (shown during searches/saves)
    // to disappear, since checking the table while it's still loading is
    // what causes both the StaleElementReferenceException and the
    // intermittent timeouts we were seeing
    private void waitForSpinnerToDisappear() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".oxd-loading-spinner")));
        } catch (Exception ignored) {
            // spinner may never appear if the action was fast enough - that's fine
        }
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".oxd-loading-spinner")));
        } catch (Exception ignored) {
            // if there's no spinner class match on this OrangeHRM version, don't block the flow
        }
    }

    // Generic retry wrapper: retries the given action up to 3 times if it
    // throws StaleElementReferenceException, with a short pause between
    // attempts - absorbs Angular's extra re-render cycles
    private <T> T withStaleRetry(java.util.concurrent.Callable<T> action) {
        int maxAttempts = 3;
        StaleElementReferenceException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.call();
            } catch (StaleElementReferenceException e) {
                lastException = e;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw lastException;
    }
}