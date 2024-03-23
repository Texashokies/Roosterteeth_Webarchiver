package com.roosterteeth.pages.rtstore;

import com.roosterteeth.pages.RoosterteethPage;
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
        if (!driver.getCurrentUrl().equals(url)) {
            driver.get(url);
        }

        WaitHelper.waitForPageReady(Duration.ofSeconds(10),driver);

        List<WebElement> rewardsPopup = driver.findElements(By.xpath("//iframe[contains(@class,'smile-prompt-frame')]"));
        if(!rewardsPopup.isEmpty()){
            driver.switchTo().frame(rewardsPopup.getFirst());
            driver.findElement(By.xpath("//button[@class='system-btn close prompt-close-btn']")).click();
            driver.switchTo().parentFrame();
        }

        //Get number of reviews
        int numReviews;
        try {
            numReviews = Integer.parseInt(driver.findElement(By.xpath("//span[@class='pr-snippet-review-count']")).getText().replace(" Reviews",""));
        }catch (NumberFormatException e){
            numReviews = 0;
        }

        if(numReviews > 10){
            List<WebElement> nextButton = driver.findElements(By.xpath("//a[contains(@class,'pr-rd-pagination-btn--next')]"));
            while(!nextButton.isEmpty()){
                Actions actions = new Actions(driver);
                actions.scrollToElement(nextButton.getFirst()).build().perform();
                Long scrollPosition = (Long) ((JavascriptExecutor)driver).executeScript("return document.documentElement.scrollTop;");
                try{
                    nextButton.getFirst().click();
                }catch (ElementClickInterceptedException e){
                    actions.scrollToElement(nextButton.getFirst()).build().perform();
                    nextButton.getFirst().click();
                } catch (StaleElementReferenceException e){
                    nextButton = driver.findElements(By.xpath("//a[contains(@class,'pr-rd-pagination-btn--next')]"));
                    continue;
                }
                WaitHelper.waitForPageToScroll(Duration.ofSeconds(10),driver,scrollPosition);
                //Wait for scroll
                nextButton = driver.findElements(By.xpath("//a[contains(@class,'pr-rd-pagination-btn--next')]"));
            }
        }

    }

    @Override
    public Set<String> getFoundUnarchivedURLS(){
        List<WebElement> productLinks = driver.findElements(By.xpath("//div[contains(@class,'product-block block')]/div/div/a"));

        HashSet<String> unArchivedProducts = new HashSet<>();

        for(WebElement product: productLinks){
            String url = StringUtils.substringBefore("https://store.roosterteeth.com" + product.getAttribute("href"),"?");
            if(!excludedURLS.contains(url)){
                unArchivedProducts.add(url);
            }
        }

        return unArchivedProducts;
    }
}
