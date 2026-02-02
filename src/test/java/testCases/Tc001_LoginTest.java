package testCases;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.ClickOnLogin;


public class Tc001_LoginTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(Tc001_LoginTest.class);


    //click on login/signup button
    @Test(priority = 1)
    public void loginTest() {
        logger.info("Click on login/signup button test started");
        ClickOnLogin lp = new ClickOnLogin(driver);
        lp.clickOnLoginPageButton();
        Assert.assertTrue(true);
    }



}
