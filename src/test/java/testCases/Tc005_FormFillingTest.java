package testCases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.FillingFormAndCapture;

public class Tc005_FormFillingTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(Tc005_FormFillingTest.class);


    @Test(priority = 1)
    public void formFillingTest() {
        logger.info("Form Filling and Capture Screenshot test started");
        FillingFormAndCapture ffc = new FillingFormAndCapture(driver);
        ffc.enterName("Suraj");
        ffc.enterOrganizationName("Cognizant");
        ffc.enterContactNumber("630494836");
        ffc.enterOfficialEmailId("Shaiksuraj113@gmail.com");
        ffc.selectOrganizationSize("501-1000");
        ffc.selectIntrestedIn("Taking a demo");
        boolean isButtonEnabled = ffc.isEnabledScheduleDemoButton();
        if (!isButtonEnabled){
            ffc.takeScreenshot("C:\\Users\\2457259\\OneDrive - Cognizant\\Desktop\\FindingHospitals\\Screenshot\\screenshot2.png");
            Assert.assertFalse(isButtonEnabled,"Button is not enabled");
        }
        // Navigate back to the previous page, After taking screenshot
        ffc.gotoPreviousPage();

    }
}
