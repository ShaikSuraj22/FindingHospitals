package pageObjects;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListOfHospitals extends BasePage {

    //Constructor
    public ListOfHospitals(WebDriver driver) {
        super(driver);
    }

    //Locators
    By clickOnCardiologyHospitalsFilter = By.xpath("//div[@class='u-spacer--medium-top']//a[10]");
    By hospitalCards = By.xpath("//div[contains(@class,'c-estb-card')]");
    By nameInCard = By.cssSelector(".c-estb-info h2");
    By ratingInCard = By.xpath("//div[@class='text-1']//span");
    //   Correct locator (TEXT, NOT title)
    //   after scrolling and waiting for this particular hospital to appear and click on it for loading many hospital names
    By hospital = By.xpath("//h2[normalize-space()='Brains Super Speciality Hospital']");



    //Methods

    //Get list of hospitals open 24x7 with rating above 3.5
    public List<String> getHospitalsOpen247WithRatingAbove35() {

        // Wait for hospital cards to load
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(hospitalCards));

        // Find all hospital cards
        List<WebElement> cards = driver.findElements(hospitalCards);

        // List to store qualified hospital names
        List<String> qualified = new ArrayList<>();

        // Iterate through each card
        for (int i = 0; i < cards.size(); i++) {
            //In each card, find name, rating and 24x7 status
            WebElement card = cards.get(i);
            WebElement rating = card.findElement(ratingInCard);
            WebElement nameEl = card.findElement(nameInCard);
            String hospitalName = nameEl.getText().trim();
            String rate = rating.getText().trim();

            // Check if rating is above 3.5
            double val = Double.parseDouble(rate);
            if (val > 3.5) {
                // Add hospital name to the list
                qualified.add(hospitalName);
            }
        }
        return qualified;

    }

    //Click on Cardiology Hospitals filter
    public void clickOnCardiologyHospitalsFilter() {
        driver.findElement(clickOnCardiologyHospitalsFilter).click();
    }

    // Check if specific hospital is showing and click on it
    public void isHospitalShowing() {
        //  Explicit wait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
        //  Javascript Executor
        JavascriptExecutor js = (JavascriptExecutor) driver;

        //   Force scroll to load hospital cards
        for (int i = 0; i < 12; i++) {
            // scrolling down by 600 pixels in each iteration to load more content dynamically.
            js.executeScript("window.scrollBy(0,600);");
            try {
                Thread.sleep(700); // allow lazy load
            } catch (InterruptedException ignored) {
            }
        }

        //  Wait for element to appear in DOM
        wait.until(ExpectedConditions.presenceOfElementLocated(hospital));

        //  Scroll element into view
        WebElement el = driver.findElement(hospital);
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);

        // Click safely
        wait.until(ExpectedConditions.elementToBeClickable(hospital)).click();

    }

    // Now after clicking on hospital, switch to new tab and close it to go back to previous page
    public void backToPreviousPage() {
        Set<String> s = driver.getWindowHandles();
        List<String> li = new ArrayList<>(s);
        driver.switchTo().window(li.get(1));
        //closing the new tab
        driver.close();
        //Switching back to previous tab
        driver.switchTo().window(li.get(0));
        //Checking whether we are back to previous page, Now no need
//        driver.findElement(By.xpath("//h2[@title='Pathway Hospitals']")).click();
    }

    //Writing hospital names to Excel file
    public void writeHospitalNamesToExcel() throws IOException {
        List<String> hospitalNames = getHospitalsOpen247WithRatingAbove35();
        FileOutputStream file = new FileOutputStream("C:\\Users\\2457259\\OneDrive - Cognizant\\Desktop\\FindingHospitals\\ExcelData\\HospitalsList.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet1");
        for(int i=0;i<hospitalNames.size();i++){
            sheet.createRow(i).createCell(0).setCellValue(hospitalNames.get(i));
        }
        workbook.write(file);
        workbook.close();
        file.close();
    }


}
