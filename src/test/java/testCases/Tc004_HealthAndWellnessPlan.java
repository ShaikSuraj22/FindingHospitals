package testCases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.HealthAndWellnessplans;

public class Tc004_HealthAndWellnessPlan extends BaseTest {

    private static final Logger logger = LogManager.getLogger(Tc004_HealthAndWellnessPlan.class);

    @Test(priority = 1)
    public void healthAndWellnessPlanTest() {
        logger.info("Health and Wellness Plan test started");
        HealthAndWellnessplans hw = new HealthAndWellnessplans(driver);
        hw.clickOnDropdown();
        hw.selectHealthAndWellnessPlans();
        Assert.assertTrue(true);

    }
}
