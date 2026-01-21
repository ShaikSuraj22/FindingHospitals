package testCases;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.*;

import java.time.Duration;

public class BaseTest {


    //Create the driver
    protected static WebDriver driver;

    @BeforeSuite(alwaysRun = true)
    public void createDriverOnce() {
        if (driver == null) {
            driver = new ChromeDriver();
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.get("https://www.practo.com/");
        }
    }

//    @AfterSuite(alwaysRun = true)
    public void tearDownOnce() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }


}
