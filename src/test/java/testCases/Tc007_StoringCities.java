package testCases;

import org.testng.annotations.Test;
import pageObjects.StoringCities;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Tc007_StoringCities extends BaseTest{

    StoringCities sc;

    //click on logo and store city names into excel
    @Test(priority = 1)
    public void clickOnLogoTest() throws IOException {
        sc = new StoringCities(driver);
        sc.clickOnLogo();
        sc.clickOnLabTests();
        sc.storingCityNamesIntoList();
        sc.pushingNamesIntoExcel();
        sc.clickOnLogo();
    }




}
