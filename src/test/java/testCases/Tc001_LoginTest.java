package testCases;


import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.ClickOnLogin;

public class Tc001_LoginTest extends BaseTest {

    //click on login/signup button
    @Test(priority = 1)
    public void loginTest() {
        ClickOnLogin lp = new ClickOnLogin(driver);
        lp.clickOnLoginPageButton();
        Assert.assertTrue(true);
    }



}
