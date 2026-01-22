package testCases;

import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.FillingFormAndCapture;

public class Tc005_FormFillingTest extends BaseTest {

    @Test(priority = 1)
    public void formFillingTest() {
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
        ffc.gotoPreviousPage();

    }
}
