package testCases;

import org.testng.annotations.Test;
import pageObjects.ListOfHospitals;

import java.io.IOException;

public class Tc006_ListOfHospitals extends BaseTest  {

    ListOfHospitals loh;

    //Getting list of hospitals which are open 24/7 with
    // rating above 3.5 and storing them in a list
    @Test(priority = 3)
    public void testListOfHospitals() {
        loh = new ListOfHospitals(driver);
        loh.getHospitalsOpen247WithRatingAbove35();
    }

    //Clicking on Cardiology hospitals filter
    @Test(priority = 1)
    public void testFilterCardiologyHospitals() {
        loh = new ListOfHospitals(driver);
        loh.clickOnCardiologyHospitalsFilter();
    }

    //Verifying whether hospitals are displayed or not
    // and scrolling up to target hospital and clicking on it and back to previous page
    @Test(priority = 2)
    public void isdisplayed() {
        loh = new ListOfHospitals(driver);
        loh.isHospitalShowing();
        loh.backToPreviousPage();
    }

    //Writing hospital names to Excel file
    // by calling the method of list storing hospital names
    @Test(priority = 4)
    public void writeIntoExcel() throws IOException {
        loh = new ListOfHospitals(driver);
        loh.writeHospitalNamesToExcel();
    }
}
