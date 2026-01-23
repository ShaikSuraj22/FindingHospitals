package pageObjects;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StoringCities extends BasePage {

    //Constructor
    public StoringCities(WebDriver driver) {
        super(driver);
    }

    //Locators
    By clickOnLogo = By.xpath("//span[@class='practo-logo']//a");
    By clickOnLabTests = By.xpath("//a[@href='https://www.practo.com/tests']");

    public void clickOnLogo() {
        driver.findElement(clickOnLogo).click();
    }
    public void clickOnLabTests() {
        driver.findElement(clickOnLabTests).click();
    }

//    public void clickOnSelectCity() {
//        new WebDriverWait(driver, Duration.ofSeconds(10))
//                .until(ExpectedConditions.presenceOfElementLocated(selectCity)).click();
////        driver.findElement(selectCity).click();
//    }

    public List<String> storingCityNamesIntoList() {
        List<String> cityNames = new ArrayList<>();
        for (int i = 1; i <100 ; i++) {
            WebElement name = driver.findElement(By.xpath("//div[@class='u-margintb--std--half o-f-color--subtle u-font-bold']//following-sibling::div["+i+"]"));
            String cityName = name.getText();
            System.out.println(cityName);
            cityNames.add(cityName);
        }
        return cityNames;

    }

    public void pushingNamesIntoExcel() throws IOException {
        List<String> cities = storingCityNamesIntoList();
        FileOutputStream file = new FileOutputStream("C:\\Users\\2457259\\OneDrive - Cognizant\\Desktop\\FindingHospitals\\ExcelData\\CityNames.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("CityNames");
        for (int i=0;i<cities.size();i++) {
            XSSFRow row = sheet.createRow(i);
            row.createCell(0).setCellValue(cities.get(i));

        }
        workbook.write(file);
        workbook.close();
        file.close();
    }




}
