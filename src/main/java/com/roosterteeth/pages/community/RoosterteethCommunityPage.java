package com.roosterteeth.pages.community;

import com.roosterteeth.pages.RoosterteethPage;
import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.utility.ScrollerUtility;
import com.roosterteeth.utility.WaitHelper;
import org.junit.Ignore;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class RoosterteethCommunityPage extends RoosterteethPage {

    private static final String POST_XPATH = "//div[contains(@class,'wdio-community-feed-post-')]";
    private static final String SHOW_MORE_POSTS_XPATH = "//div[@class='community-feed__options']/button";
    private static final String SHOW_MORE_FOLLOWS_XPATH = "//div[@class='community-user-list__options']/button";

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

        archivePosts();
    }

    HashSet<String> foundUrls = new HashSet<>();

    private static final String USER_XPATH = "//div[@class='community-user-list__user']";

    public void archiveFollows(int expectedFollows){
        if(expectedFollows != 0){
            WaitHelper.waitForElementExistence(By.xpath(USER_XPATH),Duration.ofSeconds(5),driver);
            List<WebElement> follows = driver.findElements(By.xpath(USER_XPATH));
            if(follows.size() < expectedFollows){
                List<WebElement> showMorePostsButton = driver.findElements(By.xpath(SHOW_MORE_FOLLOWS_XPATH));
                while(!showMorePostsButton.isEmpty()){
                    showMorePostsButton = driver.findElements(By.xpath(SHOW_MORE_FOLLOWS_XPATH));
                    ScrollerUtility.scrollToBottomOfElement(driver.findElement(By.xpath("//div[@class='rt-halves__half half--right']")),driver);
                    try{
                        showMorePostsButton.getFirst().click();
                    }catch (StaleElementReferenceException | NoSuchElementException ex){
                        //Show button probably disappeared because of scroll.
                    }
                }
            }

            if(foundUrls.size() != expectedFollows){
                try{
                    WaitHelper.waitForElementCountToBe(By.xpath(USER_XPATH),Duration.ofSeconds(5),expectedFollows,driver);
                }catch(TimeoutException ex){
                    //Just continue
                }
            }

            List<WebElement> userNames = driver.findElements(By.xpath("//div[@class='community-user-list__user-username']"));
            for(WebElement userName: userNames){
                foundUrls.add("https://roosterteeth.com/g/user/" + userName.getText());
            }
        }
    }

    public void archivePosts(){

        //Get list of all posts
        try{
            WaitHelper.waitForElementExistence(By.xpath(POST_XPATH),Duration.ofSeconds(5),driver);
        }catch (TimeoutException e){

        }
        List<WebElement> posts = driver.findElements(By.xpath(POST_XPATH));

        if(posts.size() >= 20){
            WaitHelper.waitForElementExistence(By.xpath(SHOW_MORE_POSTS_XPATH), Duration.ofSeconds(10),driver);
            List<WebElement> showMorePostsButton = driver.findElements(By.xpath(SHOW_MORE_POSTS_XPATH));
            while(!showMorePostsButton.isEmpty()){
                LogUtility.logInfo("Clicking show more posts button!");
                showMorePostsButton = driver.findElements(By.xpath(SHOW_MORE_POSTS_XPATH));
                ScrollerUtility.scrollToBottomOfElement(driver.findElement(By.xpath("//div[@class='rt-halves__half half--right']")),driver);
                try{
                    showMorePostsButton.getFirst().click();
                }catch (StaleElementReferenceException | NoSuchElementException e){
                    //Show button probably disappeared because of scroll.
                }
                try{
                    WaitHelper.waitForElementExistence(By.xpath(SHOW_MORE_POSTS_XPATH),Duration.ofSeconds(5),driver);
                }catch (TimeoutException e){

                }
            }
        }

        List<WebElement> postUserNames = driver.findElements(By.xpath("//div[@class='community-feed__post-username']"));
        for(WebElement poster: postUserNames){
            foundUrls.add("https://roosterteeth.com/g/user/" + poster.getText().replace("\\s+",""));
        }
        List<WebElement> repostUserNames = driver.findElements(By.xpath("//span[@class='community-feed__marker-username']"));
        for(WebElement reposter:repostUserNames){
            foundUrls.add("https://roosterteeth.com/g/user/" + reposter.getText().replace("\\s+",""));
        }


        //Iterate over list opening comments
        List<WebElement> commentButtons = driver.findElements(By.xpath("//div[@class='community-feed__option community-feed__option--comment ']"));
        for (int i = 0; i < commentButtons.size(); i++) {
            WebElement commentButton = commentButtons.get(i);
            Actions actions = new Actions(driver);
            actions.scrollToElement(commentButton).build().perform();
            commentButton.click();
            WebElement commentCountElement = driver.findElement(By.xpath("(//div[contains(@class,'community-feed__option--comment')]//span[@class='community-feed__option-text'])[" + (i+1) +"]"));
            int commentCount;
            try{
              commentCount = Integer.parseInt(commentCountElement.getText().replace(" ",""));
            }catch (NumberFormatException e){
                commentCount =0;
            }
            archivePostComments(commentCount);
        }


        LogUtility.logInfo("End of archive page!");
    }

    private static final String COMMENT_MODAL_XPATH = "//div[contains(@class,'modal-post-details')]";
    private static final String COMMENT_XPATH = "//article[@class='comment row ']";
    public void archivePostComments(int commentCount){
        WaitHelper.waitForElementExistence(By.xpath(COMMENT_MODAL_XPATH),Duration.ofSeconds(5),driver);
        WaitHelper.waitForElementVisible(By.xpath(COMMENT_MODAL_XPATH),Duration.ofSeconds(5),driver);
        if(commentCount != 0){
            //Click show more for comments
            List<WebElement> comments = driver.findElements(By.xpath(COMMENT_XPATH));
            if(comments.size() >= 20){
                List<WebElement> showMoreButton = driver.findElements(By.xpath("//div[contains(@class,'comment-container-show-more')]/a"));
                while(!showMoreButton.isEmpty()){
                    showMoreButton = driver.findElements(By.xpath("//div[contains(@class,'comment-container-show-more')]/a"));
                    ScrollerUtility.scrollToBottomOfElement(driver.findElement(By.xpath("//div[@class='comment-container ']")),driver);
                    showMoreButton.getFirst().click();
                }
            }
            if(commentCount != comments.size() && driver.findElements(By.xpath("//button[@class='comment-replies-toggle']")).isEmpty()){
                //Wait in case some comments are taking time to load.
                try {
                    WaitHelper.waitForElementCountToBe(By.xpath(COMMENT_XPATH),Duration.ofSeconds(5),commentCount,driver);
                }catch (TimeoutException e){
                    //Just continue.
                }
            }
            if(commentCount != comments.size()){
                List<WebElement> showReplies = driver.findElements(By.xpath("//button[@class='comment-replies-toggle']"));
                for (int i = 0; i < showReplies.size(); i++) {
                    WebElement showReplyButton = showReplies.get(i);
                    Actions actions = new Actions(driver);
                    try {
                        actions.scrollToElement(showReplyButton).build().perform();
                        showReplyButton.click();
                    } catch (StaleElementReferenceException e) {
                        showReplies = driver.findElements(By.xpath("//button[@class='comment-replies-toggle']"));
                        i--;
                    }
                }
            }
            List<WebElement> commenters = driver.findElements(By.xpath("//h6/a[not(contains(@class,'comment__timeago '))]"));
            for (WebElement commenter:commenters){
                foundUrls.add(commenter.getAttribute("href"));
            }
        }


        //Close out comment modal
        WebElement closeButton = driver.findElement(By.xpath("//span[contains(@class,'modal-close')]"));
        Actions actions = new Actions(driver);
        actions.scrollToElement(closeButton).click().build().perform();
        WaitHelper.waitForElementNotExist(By.xpath(COMMENT_MODAL_XPATH),Duration.ofSeconds(5),driver);
    }

    @Override
    public Set<String> getFoundUnarchivedURLS() {
        return foundUrls;
    }

}
