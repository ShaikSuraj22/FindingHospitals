package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;


public class HealthAndWellnessplans extends BasePage {

    //Constructor
    public HealthAndWellnessplans(WebDriver driver){
        super(driver);
    }

    //Locators
    By clickOnDropdown = By.xpath("//span[text()='For Corporates']");
    By selectHealthAndWellnessPlans = By.xpath("//a[text()='Health & Wellness Plans']");


    //Methods
    public void clickOnDropdown() {
        driver.findElement(clickOnDropdown).click();
    }

    public void selectHealthAndWellnessPlans() {
        driver.findElement(selectHealthAndWellnessPlans).click();
    }





}
