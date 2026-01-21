package testCases;

import org.testng.annotations.Test;
import pageObjects.SearchFunctionality;

public class Tc003_SearchFunctionalityTest extends BaseTest {

    @Test(priority = 5)
    public void  locationAndSearchHospitalTest() {
        SearchFunctionality sf = new SearchFunctionality(driver);
        sf.enterLocation("Banga");
        sf.selectSuggestion();
        sf.enterHospitalName("Hospi");
        sf.selectHospitalSuggestion();
    }

}
