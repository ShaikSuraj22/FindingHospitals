package testCases;

import org.testng.annotations.Test;
import pageObjects.FillingFormAndCapture;

public class Tc005_FormFillingTest extends BaseTest {

    @Test(priority = 4)
    public void formFillingTest() {
        FillingFormAndCapture ffc = new FillingFormAndCapture(driver);
        ffc.enterName("Suraj");
        ffc.enterOrganizationName("Cognizant");
        ffc.enterContactNumber("630494836");
        ffc.enterOfficialEmailId("Shaiksuraj113@gmail.com");
        ffc.selectOrganizationSize("501-1000");
        ffc.selectIntrestedIn("Taking a demo");
        ffc.takeScreenshot("C:\\Users\\2457259\\OneDrive - Cognizant\\Desktop\\FindingHospitals\\Screenshot\\screenshot1.png");
        ffc.gotoPreviousPage();

    }
}
