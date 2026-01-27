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

    //Methods

    //Clicking on logo, for like going to home page
    public void clickOnLogo() {
        driver.findElement(clickOnLogo).click();
    }
    //Clicking on Lab Tests
    public void clickOnLabTests() {
        driver.findElement(clickOnLabTests).click();
    }

    //Storing city names into a list
    public List<String> storingCityNamesIntoList() {
        List<String> cityNames = new ArrayList<>();
        for (int i = 1; i <100 ; i++) {
            // here i is used to iterate through the list of cities
            // for every city having the same xpath, so what i have done is that, storing first sibling xpath in a variable
            // and use the index to iterate through all the siblings
            WebElement name = driver.findElement(By.xpath("//div[@class='u-margintb--std--half o-f-color--subtle u-font-bold']//following-sibling::div["+i+"]"));
            String cityName = name.getText();
            System.out.println(cityName);
            cityNames.add(cityName);
        }
        return cityNames;

    }

    //Pushing city names into excel
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
