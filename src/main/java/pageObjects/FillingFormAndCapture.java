package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.File;

public class FillingFormAndCapture extends  BasePage{

    //Constructor
    public FillingFormAndCapture(WebDriver driver) {
        super(driver);
    }

    //Locators
    By inputName = By.xpath("//input[@id='name']");
    By inputOrganizationName = By.xpath("//input[@id='organizationName']");
    By inputContactNumber = By.xpath("//input[@id='contactNumber']");
    By inputOfficialEmailId = By.xpath("//input[@id='officialEmailId']");
    By organizationSize = By.xpath("//select[@id='organizationSize']");
    By intrestedIn = By.xpath("//select[@id='interestedIn']");
    By scheduleDemoButton = By.xpath("//button[text()='Schedule a demo']");
    Select select;

    //Methods

    //Entering details in the form
    public void enterName(String name) {
        driver.findElement(inputName).sendKeys(name);
    }

    //Entering organization name
    public void enterOrganizationName(String organizationName) {
        driver.findElement(inputOrganizationName).sendKeys(organizationName);
    }

    //Entering contact number
    public void enterContactNumber(String contactNumber) {
        driver.findElement(inputContactNumber).sendKeys(contactNumber);
    }

    //Entering official email id
    public void enterOfficialEmailId(String officialEmailId) {
        driver.findElement(inputOfficialEmailId).sendKeys(officialEmailId);
    }

    //Selecting organization size from dropdown
    public void selectOrganizationSize(String size) {
        select = new Select(driver.findElement(organizationSize));
        select.selectByVisibleText(size);
    }

    //Selecting interested in from dropdown
    public void selectIntrestedIn(String interest) {
        select = new Select(driver.findElement(intrestedIn));
        select.selectByVisibleText(interest);
    }

    //Taking screenshot of the filled form, when schedule demo button is not enabled
    public void takeScreenshot(String filePath) {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File f = ts.getScreenshotAs(OutputType.FILE);
        File file = new File(filePath);
        f.renameTo(file);
    }

    //Checking if schedule demo button is enabled
    public boolean isEnabledScheduleDemoButton() {
        return driver.findElement(scheduleDemoButton).isEnabled();
    }

    //Navigating back to previous page
    public void gotoPreviousPage() {
        driver.navigate().back();
    }

}
