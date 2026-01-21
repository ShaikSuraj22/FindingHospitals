package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ClickOnLogin extends BasePage {

    //Constructor
    public ClickOnLogin(WebDriver driver) {
        super(driver);
    }

    //Locators
    By loginPageButton = By.xpath("//a[@name='Practo login']");


    //Methods
    public void clickOnLoginPageButton() {
        driver.findElement(loginPageButton).click();
    }


}
