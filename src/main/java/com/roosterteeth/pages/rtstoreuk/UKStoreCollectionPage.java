package com.roosterteeth.pages.rtstoreuk;

import com.roosterteeth.pages.RoosterteethPage;
import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.utility.WaitHelper;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UKStoreCollectionPage extends RoosterteethPage {
    public UKStoreCollectionPage(String url, WebDriver driver, HashSet<String> excludedURLS) {
        super(url, driver, excludedURLS);
    }

    private static final String PRODUCT_XPATH = "//div[contains(@class,'grid-item-border')]";

    @Override
    public void archivePage() {
        //Click show more for posts
        driver.get(url);

        WaitHelper.waitForPageReady(Duration.ofSeconds(10), driver);

        List<WebElement> cookieBannerButton = driver.findElements(By.xpath("//button[contains(@class,'cookie-popup__btn')]"));
        if(!cookieBannerButton.isEmpty()){
            cookieBannerButton.getFirst().click();
        }

        List<WebElement> paginationDots = driver.findElements(By.xpath("//ul[@class='pagination-page']/li"));
        final int numDots = paginationDots.size();
        do{
            paginationDots = driver.findElements(By.xpath("//ul[@class='pagination-page']/li"));

            List<WebElement> products = driver.findElements(By.xpath(PRODUCT_XPATH));

            for (int i = 0; i < products.size(); i++) {
                final String THIS_PRODUCT_XPATH = PRODUCT_XPATH + "[" + (i+1) + "]";
                WebElement product = products.get(i);
                Actions actions = new Actions(driver);
                actions.scrollToElement(product).moveToElement(product).build().perform();

                //ARchive swatches
                List<WebElement> swatches = driver.findElements(By.xpath(THIS_PRODUCT_XPATH +"//ul[@class='item-swatch']/li"));
                if(!swatches.isEmpty()){
                    for(WebElement swatch : swatches){
                        swatch.click();
                    }
                }

                WebElement quickViewButton = driver.findElement(By.xpath(THIS_PRODUCT_XPATH + "//a[@class='ga-event-click']"));
                try{
                    WaitHelper.waitForElementVisible(By.xpath(THIS_PRODUCT_XPATH + "//a[@class='ga-event-click']"),Duration.ofSeconds(10),driver);
                }catch (TimeoutException e){
                    actions.scrollToElement(product).moveToElement(product).build().perform();
                    WaitHelper.waitForElementVisible(By.xpath(THIS_PRODUCT_XPATH + "//a[@class='ga-event-click']"),Duration.ofSeconds(10),driver);
                }
                archiveProductQuickview(quickViewButton);
            }

            if(!paginationDots.isEmpty()){
                paginationDots.getLast().click(); //Click next button
                driver.navigate().refresh();
                WaitHelper.waitForPageReady(Duration.ofSeconds(10), driver);
                paginationDots = driver.findElements(By.xpath("//ul[@class='pagination-page']/li"));
            }
        }while(!paginationDots.isEmpty() && !paginationDots.get(numDots-2).getAttribute("class").equals("active"));



    }

    private void archiveProductQuickview(WebElement quickViewButton) {
        quickViewButton.click();
        WaitHelper.waitForElementVisible(By.xpath("//div[@class='quick-view']"),Duration.ofSeconds(10),driver);

        try{
            WaitHelper.waitForElementVisible(By.xpath("//div[@class='quick-view']//div[@class='more-view-wrapper']//li/a"),Duration.ofSeconds(3),driver);
        }catch (TimeoutException e){

        }
        List<WebElement> pictures = driver.findElements(By.xpath("//div[@class='quick-view']//div[@class='more-view-wrapper']//li/a"));
        for (int j = 1; j < pictures.size(); j++) {
            WebElement picture = pictures.get(j);
            final String productImageXpath = "//div[@class='quick-view']//div[contains(@class,'quickview-featured-image')]//img";
            String previousImage = driver.findElement(By.xpath(productImageXpath)).getAttribute("src");
            try{
                WaitHelper.waitForElementVisible(By.xpath("(//div[@class='quick-view']//div[@class='more-view-wrapper']//li/a)[" + (j+1) + "]"),Duration.ofSeconds(10),driver);
            }catch (TimeoutException e){
                //Click next button
                driver.findElement(By.xpath("//div[@class='owl-next']")).click();
                WaitHelper.waitForElementVisible(By.xpath("(//div[@class='quick-view']//div[@class='more-view-wrapper']//li/a)[" + (j+1) + "]"),Duration.ofSeconds(10),driver);
            }
            picture.click();
            try{
                WaitHelper.waitForElementAttributeToChange(By.xpath(productImageXpath), "src", previousImage, Duration.ofSeconds(10), driver);
            }catch (TimeoutException e){
                LogUtility.logInfo("Trying to see picture: " + j);
            }
        }
        driver.findElement(By.xpath("//a[contains(@class,'quickview-close-window')]")).click();
        WaitHelper.waitForElementInvisible(By.xpath("//div[@class='quick-view']"),Duration.ofSeconds(10),driver);
    }

    @Override
    public Set<String> getFoundUnarchivedURLS(){
        List<WebElement> productLinks = driver.findElements(By.xpath(PRODUCT_XPATH + "//a[@class='product-grid-image']"));

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
