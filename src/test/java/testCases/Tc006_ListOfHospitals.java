package testCases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.ListOfHospitals;

import java.io.IOException;

public class Tc006_ListOfHospitals extends BaseTest  {

    private static final Logger logger = LogManager.getLogger(Tc006_ListOfHospitals.class);


    ListOfHospitals loh;

    //Getting list of hospitals which are open 24/7 with
    // rating above 3.5 and storing them in a list
    @Test(priority = 3)
    public void testListOfHospitals() {
        logger.info("List of Hospitals test started");
        loh = new ListOfHospitals(driver);
        loh.getHospitalsOpen247WithRatingAbove35();
        Assert.assertTrue(true);
    }

    //Clicking on Cardiology hospitals filter
    @Test(priority = 1)
    public void testFilterCardiologyHospitals() {
        logger.info("Filter Cardiology Hospitals test started");
        loh = new ListOfHospitals(driver);
        loh.clickOnCardiologyHospitalsFilter();
        Assert.assertTrue(true);
    }

    //Verifying whether hospitals are displayed or not
    // and scrolling up to target hospital and clicking on it and back to previous page
    @Test(priority = 2)
    public void isdisplayed() throws InterruptedException {
        logger.info("Is Displayed test started");
        loh = new ListOfHospitals(driver);
        loh.isHospitalShowing();
        // On clicking on target hospital, and back to previous page by closing the tab
        Thread.sleep(3000);
        loh.backToPreviousPage();
        Assert.assertTrue(true);
    }

    //Writing hospital names to Excel file
    // by calling the method of list storing hospital names
    @Test(priority = 4)
    public void writeIntoExcel() throws IOException {
        logger.info("Writing Hospital Names to Excel test started");
        loh = new ListOfHospitals(driver);
        loh.writeHospitalNamesToExcel();
        Assert.assertTrue(true);
    }
}
