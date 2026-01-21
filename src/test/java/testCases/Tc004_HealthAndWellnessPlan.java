package testCases;

import org.testng.annotations.Test;
import pageObjects.HealthAndWellnessplans;

public class Tc004_HealthAndWellnessPlan extends BaseTest {

    @Test(priority = 3)
    public void healthAndWellnessPlanTest() {
        HealthAndWellnessplans hw = new HealthAndWellnessplans(driver);
        hw.clickOnDropdown();
        hw.selectHealthAndWellnessPlans();
    }
}
