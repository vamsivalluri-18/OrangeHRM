package basePage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public class BaseClass {

    private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public Properties p;
    public Logger logger = LogManager.getLogger(this.getClass());

    public static WebDriver getDriver() {
        return tlDriver.get();
    }

    // Runs before EVERY @Test method - gives each test its own fresh
    // browser session, eliminating shared-state issues between tests
    @BeforeMethod(alwaysRun = true)
    @Parameters({ "os", "browser" })
    public void setup(@Optional("windows") String os, @Optional("chrome") String br) throws IOException {

        FileReader file = new FileReader("./src/test/resources/config.properties");
        p = new Properties();
        p.load(file);

        WebDriver driver;

        if (p.getProperty("execution_env").equalsIgnoreCase("remote")) {
            String huburl = p.getProperty("huburl");
            DesiredCapabilities cap = new DesiredCapabilities();

            switch (br.toLowerCase()) {
                case "chrome":
                    cap.setBrowserName("chrome");
                    break;
                case "firefox":
                    cap.setBrowserName("firefox");
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported remote browser: " + br);
            }
            //cap.setPlatform(org.openqa.selenium.Platform.valueOf(os.toUpperCase()));
            driver = new RemoteWebDriver(new URL(huburl), cap);

        } else {
            switch (br.toLowerCase()) {
                case "chrome":
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--remote-allow-origins=*");
                    driver = new ChromeDriver(options);
                    break;
                case "firefox":
                    driver = new FirefoxDriver();
                    break;
                case "edge":
                    driver = new EdgeDriver();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported browser: " + br);
            }
        }

        tlDriver.set(driver);

        getDriver().manage().deleteAllCookies();
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        getDriver().get(p.getProperty("url"));

        logger.info("Browser launched fresh for this test and navigated to: " + p.getProperty("url"));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownEach(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            String screenshotPath = captureScreenshot(result.getMethod().getMethodName());
            logger.error("Test Failed: " + result.getMethod().getMethodName() + " | Screenshot: " + screenshotPath);
        }

        // Close the browser after every test method now, since setup()
        // creates a new one before every method
        if (getDriver() != null) {
            getDriver().quit();
            tlDriver.remove();
            logger.info("Browser closed after test.");
        }
    }

    public String captureScreenshot(String testName) {
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String path = System.getProperty("user.dir") + "/screenshots/" + testName + "_" + timeStamp + ".png";
        try {
            if (getDriver() == null) return path;
            TakesScreenshot ts = (TakesScreenshot) getDriver();
            File source = ts.getScreenshotAs(OutputType.FILE);
            File destination = new File(path);
            org.apache.commons.io.FileUtils.copyFile(source, destination);
        } catch (IOException e) {
            logger.error("Failed to capture screenshot: " + e.getMessage());
        }
        return path;
    }
}