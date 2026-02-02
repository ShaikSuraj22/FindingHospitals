package testCases;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.EnterCrendentials;

import java.util.Properties;

public class Tc002_EnterCredentials extends BaseTest {

    private static final Logger logger = LogManager.getLogger(Tc002_EnterCredentials.class);



    @Test(priority = 1)
    public void enterCredentials(){
        logger.info("Entering Credentials test started");
        EnterCrendentials ec = new EnterCrendentials(driver);
        ec.enterEmail("6304948369");
        ec.enterPassword("Suraj2209@");
        ec.clickOnLoginButton();
        Assert.assertTrue(true);

    }

}
