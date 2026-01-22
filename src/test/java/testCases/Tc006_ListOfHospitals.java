package testCases;

import org.testng.annotations.Test;
import pageObjects.ListOfHospitals;

import java.io.IOException;

public class Tc006_ListOfHospitals extends BaseTest  {

    ListOfHospitals loh;

    @Test(priority = 3)
    public void testListOfHospitals() {
        loh = new ListOfHospitals(driver);
        loh.getHospitalsOpen247WithRatingAbove35();
    }


    @Test(priority = 1)
    public void testFilterCardiologyHospitals() {
        loh = new ListOfHospitals(driver);
        loh.clickOnCardiologyHospitalsFilter();
    }

    @Test(priority = 2)
    public void isdisplayed() {
        loh = new ListOfHospitals(driver);
        loh.isHospitalShowing();
        loh.backToPreviousPage();
    }

    @Test(priority = 4)
    public void writeIntoExcel() throws IOException {
        loh = new ListOfHospitals(driver);
        loh.writeHospitalNamesToExcel();
    }
}
