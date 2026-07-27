package utilities;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import basePage.BaseClass;

public class ExtentReportManager implements ITestListener {

    private static ExtentReports extent;
    private static String reportPath;
    private static String repName;
    private static final Object LOCK = new Object();
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static final AtomicInteger finishedTestBlocks = new AtomicInteger(0);

    public void onStart(ITestContext testContext) {
        synchronized (LOCK) {
            if (extent == null) {
                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                repName = "OrangeHRM_Report_" + timeStamp + ".html";
                reportPath = System.getProperty("user.dir") + "/extentReports/" + repName;

                ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
                sparkReporter.config().setDocumentTitle("OrangeHRM Automation Report");
                sparkReporter.config().setReportName("OrangeHRM Functional Testing");
                sparkReporter.config().setTheme(Theme.DARK);

                extent = new ExtentReports();
                extent.attachReporter(sparkReporter);
                extent.setSystemInfo("Application", "OrangeHRM");
                extent.setSystemInfo("Module", "PIM");
                extent.setSystemInfo("User Name", System.getProperty("user.name"));
                extent.setSystemInfo("Environment", "QA");
            }

            String os = testContext.getCurrentXmlTest().getParameter("os");
            if (os != null) {
                extent.setSystemInfo("Operating System - " + testContext.getName(), os);
            }

            String browser = testContext.getCurrentXmlTest().getParameter("browser");
            if (browser != null) {
                extent.setSystemInfo("Browser - " + testContext.getName(), browser);
            }

            List<String> includedGroups = testContext.getCurrentXmlTest().getIncludedGroups();
            if (!includedGroups.isEmpty()) {
                extent.setSystemInfo("Groups - " + testContext.getName(), includedGroups.toString());
            }
        }
    }

    private ExtentTest getOrCreateTest(ITestResult result) {
        ExtentTest t = test.get();
        if (t == null) {
            synchronized (LOCK) {
                t = extent.createTest(result.getMethod().getMethodName());
                t.assignCategory(result.getMethod().getGroups());
            }
            test.set(t);
        }
        return t;
    }

    public void onTestStart(ITestResult result) {
        synchronized (LOCK) {
            ExtentTest t = extent.createTest(result.getMethod().getMethodName());
            t.assignCategory(result.getMethod().getGroups());
            test.set(t);
        }
    }

    public void onTestSuccess(ITestResult result) {
        getOrCreateTest(result).log(Status.PASS, result.getName() + " got successfully executed");
    }

    public void onTestFailure(ITestResult result) {
        ExtentTest t = getOrCreateTest(result);
        t.log(Status.FAIL, result.getName() + " got failed");
        if (result.getThrowable() != null) {
            t.log(Status.INFO, result.getThrowable().getMessage());
        }

        try {
            String imgPath = BaseClass.getDriver() != null
                    ? new BaseClass().captureScreenshot(result.getName())
                    : null;
            if (imgPath != null) {
                t.addScreenCaptureFromPath(imgPath);
            }
        } catch (Exception e) {
            t.log(Status.WARNING, "Could not attach screenshot: " + e.getMessage());
        }
    }

    public void onTestSkipped(ITestResult result) {
        ExtentTest t = getOrCreateTest(result);
        t.log(Status.SKIP, result.getName() + " got skipped");
        if (result.getThrowable() != null) {
            t.log(Status.INFO, result.getThrowable().getMessage());
        }
    }

    public void onFinish(ITestContext testContext) {
        synchronized (LOCK) {
            extent.flush();

            // Only auto-open once ALL <test> blocks in the suite have
            // finished - opening after just the first one showed a
            // stale/incomplete snapshot before the other browser's
            // results were written
            int totalTestBlocks = testContext.getSuite().getXmlSuite().getTests().size();
            int finishedSoFar = finishedTestBlocks.incrementAndGet();

            if (finishedSoFar >= totalTestBlocks) {
                try {
                    File extentReport = new File(reportPath);
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(extentReport.toURI());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}