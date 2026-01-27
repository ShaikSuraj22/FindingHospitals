package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SearchFunctionality extends BasePage {

    //Constructor
    public SearchFunctionality(WebDriver driver) {
        super(driver);
    }

    //locators
    By locationSearchBox = By.xpath("//input[@placeholder='Search location']");
    By suggestionForLocation = By.xpath("//div[text()='Bangalore']");
    By searchHospitals = By.xpath("//input[@placeholder='Search doctors, clinics, hospitals, etc.']");
    By suggestionForHospital = By.xpath("//div[text()='Hospital']");

    //Methods

    //Entering location
    public void enterLocation(String location) {
        driver.findElement(locationSearchBox).clear();
        driver.findElement(locationSearchBox).sendKeys(location);
    }

    //Selecting location suggestion
    public void selectSuggestion() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(suggestionForLocation))
                .click();
    }

    //Entering hospital name
    public void enterHospitalName(String hospitalName) {
        driver.findElement(searchHospitals).clear();
        driver.findElement(searchHospitals).sendKeys(hospitalName);
    }

    //Selecting hospital suggestion
    public void selectHospitalSuggestion() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(suggestionForHospital))
                .click();
    }

}
