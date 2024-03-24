package com.roosterteeth.pages.rtstore;

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

public class StoreProductPage extends RoosterteethPage {
    public StoreProductPage(String url, WebDriver driver, HashSet<String> excludedURLS) {
        super(url, driver, excludedURLS);
    }

    @Override
    public void archivePage() {
        LogUtility.logInfo("Window handle: " + driver.getWindowHandle());
        LogUtility.logInfo("Page before get: " + driver.getCurrentUrl());
        driver.get(url);

        WaitHelper.waitForPageReady(Duration.ofSeconds(10),driver);

        List<WebElement> rewardsPopup = driver.findElements(By.xpath("//iframe[contains(@class,'smile-prompt-frame')]"));
        if(!rewardsPopup.isEmpty()){
            driver.switchTo().frame(rewardsPopup.getFirst());
            driver.findElement(By.xpath("//button[@class='system-btn close prompt-close-btn']")).click();
            driver.switchTo().parentFrame();
        }

        //Get number of reviews
        archiveReviews(driver);

        archiveImages(driver);

        List<WebElement> relatedProducts = driver.findElements(By.xpath("//div[contains(@class,'product-block ')]"));
        for (int i = 0; i < relatedProducts.size(); i++) {
            final String productInfo = "(//div[contains(@class,'product-block__info')])[" + (i+1) + "]";
            final String productQuickViewButton = "(//div[contains(@class,'product-block__image-inner')]/a)[" + (i+1) + "]";
            WebElement product = driver.findElement(By.xpath(productInfo));
            Actions actions = new Actions(driver);
            actions.scrollToElement(product).build().perform();
            actions.moveToElement( driver.findElement(By.xpath(productQuickViewButton))).build().perform();
            try{
                WaitHelper.waitForElementVisible(By.xpath(productQuickViewButton),Duration.ofSeconds(3),driver);
            }catch (TimeoutException e){
                actions.scrollToElement(product).build().perform();
                actions.moveToElement( driver.findElement(By.xpath(productQuickViewButton))).build().perform();
                WaitHelper.waitForElementVisible(By.xpath(productQuickViewButton),Duration.ofSeconds(3),driver);
            }

            driver.findElement(By.xpath(productQuickViewButton)).click();

            final String QUICKVIEW_XPATH = "(//div[@class='quickView'])[";
            WaitHelper.waitForElementVisible(By.xpath(QUICKVIEW_XPATH + (i+1) +"]"),Duration.ofSeconds(5),driver);

            try{
                Thread.sleep(Duration.ofSeconds(1));
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            //Get number of images in slider. This xpath also picks up the dots container so subtract 1
            List<WebElement> dots = driver.findElements(By.xpath(QUICKVIEW_XPATH + (i+1) +"]//li[contains(@class,'dot')]"));
            for (int j = 1; j < dots.size(); j++) {
                LogUtility.logInfo(String.format("Clicking on dot %d for product %d", j,i));
                WebElement dot = dots.get(j);
                try{
                    dot.click();
                }catch (ElementClickInterceptedException e){
                    dot.click();
                }
                String imageXpath = QUICKVIEW_XPATH + (i+1) +"]//div[contains(@class,'js-productImgSlider ')]//div[contains(@class,'js-slide ')][" + (j+1) + "]";
                WaitHelper.waitForElementVisible(By.xpath(imageXpath), Duration.ofSeconds(10), driver);
            }

            WebElement closeButton= driver.findElement(By.xpath(QUICKVIEW_XPATH + (i+1) +"]//a[contains(@class,'quickView-close')]"));
            actions.scrollToElement(closeButton).click(closeButton).build().perform();

            WaitHelper.waitForElementInvisible(By.xpath(QUICKVIEW_XPATH + (i+1) +"]"),Duration.ofSeconds(10),driver);
        }


        //Wait for images to resolve
        int secondsToWait = 10;
        LogUtility.logInfo(String.format("Waiting %d seconds for images to resolve archiving.", secondsToWait));
        try{
            Thread.sleep(Duration.ofSeconds(secondsToWait));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        LogUtility.logInfo("Waiting for urls done.");
    }

    private void archiveImages(WebDriver driver) {
        List<WebElement> notifcationsPopup = driver.findElements(By.xpath("//pushbox-cta//button-deny"));
        if(!notifcationsPopup.isEmpty()){
            notifcationsPopup.getFirst().click();
        }

        //Go through pictures. Clicking both small and large
        final String pictureSlideXpath =  "//div[contains(@class,'productImgSlider-nav')]//div[contains(@class,'js-slide')]";
        List<WebElement> pictureSlides = driver.findElements(By.xpath(pictureSlideXpath));
        for (int i = 0; i < pictureSlides.size(); i++) {
            LogUtility.logInfo("Click on picture slide: " + i);
            WebElement element = pictureSlides.get(i);
            //Always moving in a rightward direction
            if (!element.getAttribute("class").contains("is-selected")) {
                driver.findElement(By.xpath("//div[contains(@class,'productImgSlider-nav')]//button[contains(@class,'next')]")).click();
                WaitHelper.waitForElementToHaveClass(By.xpath(pictureSlideXpath + "[" + (i+1)+"]"),"is-selected",Duration.ofSeconds(10),driver);
            }
            Actions actions = new Actions(driver);
            actions.scrollToElement(element).build().perform();
            element.click();
            final String bigPictureXpath = "//div[contains(@class,'js-productImgSlider')]//div[contains(@class,'js-slide')][" + (i+1) + "]";
            WebElement bigPicture = driver.findElement(By.xpath(bigPictureXpath));
            bigPicture.click();
            try{
                Thread.sleep(Duration.ofSeconds(1));
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            //Big picture modal click close
            WaitHelper.waitForElementExistence(By.xpath("//div[contains(@class,'mfp-wrap')]"),Duration.ofSeconds(10),driver);
            driver.findElement(By.xpath("//div[contains(@class,'mfp-wrap')]//button[@title='Close (Esc)']")).click();
        }
    }

    private void archiveReviews(WebDriver driver) {
        int numReviews;
        try {
            numReviews = Integer.parseInt(driver.findElement(By.xpath("//span[@class='pr-snippet-review-count']")).getText().replace(" Reviews",""));
        }catch (NumberFormatException | NoSuchElementException e){
            numReviews = 0;
        }

        final String NEXT_XPATH = "//a[contains(@class,'pr-rd-pagination-btn--next')]";
        if(numReviews > 10){
            List<WebElement> nextButton = driver.findElements(By.xpath(NEXT_XPATH));
            while(!nextButton.isEmpty()){
                LogUtility.logInfo("Clicking on next review button.");
                Actions actions = new Actions(driver);
                actions.scrollToElement(nextButton.getFirst()).build().perform();
                try{
                    nextButton.getFirst().click();
                }catch (ElementClickInterceptedException e){
                    actions.scrollToElement(nextButton.getFirst()).build().perform();
                    nextButton.getFirst().click();
                } catch (StaleElementReferenceException e){
                    nextButton = driver.findElements(By.xpath(NEXT_XPATH));
                    continue;
                }
                try{
                    WaitHelper.waitForElementNotExist(By.xpath(NEXT_XPATH),Duration.ofSeconds(5),driver);
                }catch (TimeoutException e){
                    //This is fine
                }
                nextButton = driver.findElements(By.xpath(NEXT_XPATH));
            }
        }
    }

    @Override
    public Set<String> getFoundUnarchivedURLS(){
        List<WebElement> productLinks = driver.findElements(By.xpath("//div[contains(@class,'product-block block')]/div/div/a"));

        HashSet<String> unArchivedProducts = new HashSet<>();

        for(WebElement product: productLinks){
            String productLink = product.getAttribute("href");
            if(!productLink.contains("https://store.roosterteeth.com")){
               productLink ="https://store.roosterteeth.com" + productLink;
            }
            String url = StringUtils.substringBefore(productLink,"?");
            if(!excludedURLS.contains(url)){
                unArchivedProducts.add(url);
            }
        }

        return unArchivedProducts;
    }
}
