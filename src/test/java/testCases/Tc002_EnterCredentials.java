package testCases;


import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.EnterCrendentials;

public class Tc002_EnterCredentials extends BaseTest {



    @Test(priority = 1)
    public void enterCredentials(){
        EnterCrendentials ec = new EnterCrendentials(driver);
        ec.enterEmail("6304948369");
        ec.enterPassword("Suraj2209@");
        ec.clickOnLoginButton();
        Assert.assertTrue(true);

    }

}
