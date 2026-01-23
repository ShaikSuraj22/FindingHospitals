package testCases;

import org.testng.annotations.Test;
import pageObjects.StoringCities;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Tc007_StoringCities extends BaseTest{

    StoringCities sc;

    @Test(priority = 1)
    public void clickOnLogoTest() throws IOException {
        sc = new StoringCities(driver);
        sc.clickOnLogo();
        sc.clickOnLabTests();
//        sc.clickOnSelectCity();
        sc.storingCityNamesIntoList();
        sc.pushingNamesIntoExcel();
    }




}
