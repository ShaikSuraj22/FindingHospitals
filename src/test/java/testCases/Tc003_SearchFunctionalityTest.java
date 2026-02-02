package testCases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.SearchFunctionality;

public class Tc003_SearchFunctionalityTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(Tc003_SearchFunctionalityTest.class);

    @Test(priority = 1)
    public void  locationAndSearchHospitalTest() {
        logger.info("Location and Search Hospital test started");
        SearchFunctionality sf = new SearchFunctionality(driver);
        sf.enterLocation("Banga");
        sf.selectSuggestion();
        sf.enterHospitalName("Hospi");
        sf.selectHospitalSuggestion();
        Assert.assertTrue(true);
    }

}
