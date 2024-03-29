package com.roosterteeth.pages.rtstore;

import com.roosterteeth.pages.RoosterteethPage;
import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.utility.ScrollerUtility;
import com.roosterteeth.utility.WaitHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoreCollectionPage extends RoosterteethPage {

    public StoreCollectionPage(String url, WebDriver driver,HashSet<String> excludedURLS) {
        super(url, driver,excludedURLS);
    }

    private static final String PRODUCT_XPATH = "//div[contains(@class,'boost-pfs-filter-product-item ')]";

    @Override
    public void archivePage() {
        //Click show more for posts
        driver.get(url);

        WaitHelper.waitForPageReady(Duration.ofSeconds(10),driver);

        //Get number of products
        final int numProducts = Integer.parseInt(driver.findElement(By.xpath("//span[@class='boost-pfs-filter-total-product']")).getText().replace(" Products","").replace(" Product",""));

        List<WebElement> products = driver.findElements(By.xpath(PRODUCT_XPATH));
        while(products.size() < numProducts){
            LogUtility.logInfo("Scrolling down.");
            ScrollerUtility.scrollToPageBottom(driver);
            WaitHelper.waitForMoreElements(By.xpath(PRODUCT_XPATH), Duration.ofSeconds(10),products.size(),driver);
            products = driver.findElements(By.xpath(PRODUCT_XPATH));
        }

        //For each item
        for(int i = 0;i<products.size();i++){
            WebElement product = products.get(i);
            Actions actions = new Actions(driver);
            actions.scrollToElement(product).moveToElement(product).build().perform();
            WebElement quickViewButton = driver.findElement(By.xpath(PRODUCT_XPATH + "[" + (i+1) + "]//button[contains(@class,'boost-pfs-quickview-btn')]"));
            quickViewButton.click();

            WaitHelper.waitForElementVisible(By.xpath("//div[@class='boost-pfs-quickview-content']"),Duration.ofSeconds(40),driver);

            //Get number of images in slider. This xpath also picks up the dots container so subtract 1
            final int numImages = driver.findElements(By.xpath("//div[contains(@class,'boost-pfs-quickview-slider-dot')]")).size() - 1;

            for(int imageIterator = 0;imageIterator < numImages && numImages > 1; imageIterator++){
                WebElement nextImage = driver.findElement(By.xpath("//button[contains(@class,'boost-pfs-quickview-slider-next')]"));
                nextImage.click();
            }

            driver.findElement(By.xpath("//button[@title='Close']")).click();

            WaitHelper.waitForElementInvisible(By.xpath("//div[@class='boost-pfs-quickview-content']"),Duration.ofSeconds(10),driver);
        }

        //WAit some time just in case
        LogUtility.logInfo("Waiting 2 minutes");
        driver.manage().timeouts().implicitlyWait(Duration.ofMinutes(2));

    }

    @Override
    public Set<String> getFoundUnarchivedURLS(){
        List<WebElement> productLinks = driver.findElements(By.xpath(PRODUCT_XPATH + "//div[@class='boost-pfs-filter-product-item-image']/a"));

        HashSet<String> unArchivedProducts = new HashSet<>();

        for(WebElement product: productLinks){
            String url = product.getAttribute("href");
            if(!excludedURLS.contains(url)){
                unArchivedProducts.add(url);
            }
        }

        return unArchivedProducts;
    }
}