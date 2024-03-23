package com.roosterteeth.pages.community;

import com.roosterteeth.pages.RoosterteethPage;
import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.utility.WaitHelper;
import org.openqa.selenium.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;

public class RoosterteethCommunityPage extends RoosterteethPage {

    private static final String POST_XPATH = "//div[contains(@class,'wdio-community-feed-post-')]";
    private static final String SHOW_MORE_POSTS_XPATH = "//div[@class='community-feed__options']/button";

    public RoosterteethCommunityPage(String url, WebDriver driver, HashSet<String> excludedURLS) {
        super(url, driver,excludedURLS);
    }

    /**
     * Opens all community posts into comments modal, and clicks show more for posts
     */
    @Override
    public void archivePage() {
        //Click show more for posts
        if(!driver.getCurrentUrl().equals(url)) {
            driver.get(url);
        }


        WaitHelper.waitForElementExistence(By.xpath(SHOW_MORE_POSTS_XPATH), Duration.ofSeconds(10),driver);
        WebElement showMorePostsButton = driver.findElement(By.xpath(SHOW_MORE_POSTS_XPATH));
        while(showMorePostsButton != null){
            LogUtility.logInfo("Clicking show more posts button!");
            showMorePostsButton = driver.findElement(By.xpath(SHOW_MORE_POSTS_XPATH));
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollTo(0, arguments[0].scrollHeight)",driver.findElement(By.xpath("//div[@class='rt-halves__half half--right']")));
            try{
                showMorePostsButton.click();
            }catch (StaleElementReferenceException e){
                //Show button probably disappeared because of scroll.
            }

            //Check if at end
            try{
                WebElement endContainer = driver.findElement(By.xpath("//div[@class='community-feed__end-of-feed-container']"));
                if(endContainer != null){
                    break;
                }
            }catch (NoSuchElementException e){
                WaitHelper.waitForElementExistence(By.xpath(SHOW_MORE_POSTS_XPATH),Duration.ofMinutes(2),driver);
                showMorePostsButton = driver.findElement(By.xpath(SHOW_MORE_POSTS_XPATH));
            }
        }

        //Get list of all posts
        List<WebElement> posts = driver.findElements(By.xpath(POST_XPATH));

        //Iterate over list opening comments
        LogUtility.logInfo("Ebd if archive page!");
    }

    public void archivePostComments(){
        //Click show more for comments

        //Click all show replies

        //Close out comment modal
    }



}
