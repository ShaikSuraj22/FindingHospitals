package testCases;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.StoringCities;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Tc007_StoringCities extends BaseTest{

    StoringCities sc;

    private static final Logger logger = LogManager.getLogger(Tc007_StoringCities.class);

    //click on logo and store city names into excel
    @Test(priority = 1)
    public void clickOnLogoTest() throws IOException {
        logger.info("Storing Cities test started");
        sc = new StoringCities(driver);
        sc.clickOnLogo();
        sc.clickOnLabTests();
        sc.storingCityNamesIntoList();
        sc.pushingNamesIntoExcel();
        sc.clickOnLogo();
        Assert.assertTrue(true);
    }




}
