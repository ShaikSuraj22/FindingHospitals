package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class EnterCrendentials extends BasePage {

    //Constructor
    public EnterCrendentials(WebDriver driver) {
        super(driver);
    }

    //Locators
    By emailField = By.xpath("//input[@id='username']");
    By passwordField = By.xpath("//input[@id='password']");
    By loginButton = By.xpath("//button[@class='btn  btn-lg common-btn practo-btn']");


    //Methods

    //Entering email and password
    public void enterEmail(String email) {
        // Wait for the email field to be visible, then type
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(emailField))
                .sendKeys(email);
    }

    public void enterPassword(String password) {
        driver.findElement(passwordField).sendKeys(password);

    }

    //Clicking on login button
    public void clickOnLoginButton() {
        driver.findElement(loginButton).click();
    }


}
