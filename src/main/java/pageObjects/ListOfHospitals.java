package pageObjects;

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
    By hospital = By.xpath("//h2[normalize-space()='Brains Super Speciality Hospital']");
//    By open24x7Exact = By.xpath("//span[@class='pd-right-2px-text-green']");
//    By isShowingOrNot = By.xpath("//h2[normalize-space()='East Point Hospital']");


    //Methods
    public List<String> getHospitalsOpen247WithRatingAbove35() {

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(hospitalCards));

        List<WebElement> cards = driver.findElements(hospitalCards);
        List<String> qualified = new ArrayList<>();

        for (int i = 0; i < cards.size(); i++) {
            WebElement card = cards.get(i);
            WebElement rating = card.findElement(ratingInCard);
            WebElement nameEl = card.findElement(nameInCard);
//            WebElement open24By7 = card.findElement(open24x7Exact);
            String hospitalName = nameEl.getText().trim();
            String rate = rating.getText().trim();

            //This is not Working as the locator is not correct for 24x7
//            String open24 = open24By7.getText().trim();
//            System.out.println("Hospital: " + hospitalName + ", Rating: " + rate  + " "+ open24);

            double val = Double.parseDouble(rate);
            if (val > 3.5) {
                qualified.add(hospitalName);
            }
        }
        return qualified;

    }

    public void clickOnCardiologyHospitalsFilter() {
        driver.findElement(clickOnCardiologyHospitalsFilter).click();
    }

    public void isHospitalShowing() {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        //   Force scroll to load hospital cards
        for (int i = 0; i < 12; i++) {
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


    public void backToPreviousPage() {
        Set<String> s = driver.getWindowHandles();
        List<String> li = new ArrayList<>(s);
        driver.switchTo().window(li.get(1));
        driver.close();
        driver.switchTo().window(li.get(0));
        //Checking whether we are back to previous page, Now no need
//        driver.findElement(By.xpath("//h2[@title='Pathway Hospitals']")).click();
    }

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
