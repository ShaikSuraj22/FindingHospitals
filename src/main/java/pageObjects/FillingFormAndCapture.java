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
    Select select;

    //Methods
    public void enterName(String name) {
        driver.findElement(inputName).sendKeys(name);
    }

    public void enterOrganizationName(String organizationName) {
        driver.findElement(inputOrganizationName).sendKeys(organizationName);
    }

    public void enterContactNumber(String contactNumber) {
        driver.findElement(inputContactNumber).sendKeys(contactNumber);
    }

    public void enterOfficialEmailId(String officialEmailId) {
        driver.findElement(inputOfficialEmailId).sendKeys(officialEmailId);
    }

    public void selectOrganizationSize(String size) {
        select = new Select(driver.findElement(organizationSize));
        select.selectByVisibleText(size);
    }

    public void selectIntrestedIn(String interest) {
        select = new Select(driver.findElement(intrestedIn));
        select.selectByVisibleText(interest);
    }

    public void takeScreenshot(String filePath) {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File f = ts.getScreenshotAs(OutputType.FILE);
        File file = new File(filePath);
        f.renameTo(file);
    }

    public void gotoPreviousPage() {
        driver.navigate().back();
    }

}
