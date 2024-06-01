package com.roosterteeth.pages.community;

import com.roosterteeth.pages.RoosterteethPage;
import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.utility.ScrollerUtility;
import com.roosterteeth.utility.WaitHelper;
import org.junit.Ignore;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

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
                WaitHelper.waitForElementExistence(By.xpath(SHOW_MORE_FOLLOWS_XPATH),Duration.ofSeconds(5),driver);
                List<WebElement> showMorePostsButton = driver.findElements(By.xpath(SHOW_MORE_FOLLOWS_XPATH));
                while (follows.size() < expectedFollows && !showMorePostsButton.isEmpty()){
                    showMorePostsButton = driver.findElements(By.xpath(SHOW_MORE_FOLLOWS_XPATH));
                    ScrollerUtility.scrollToBottomOfElement(driver.findElement(By.xpath("//div[@class='rt-halves__half half--right']")),driver);
                    try{
                        showMorePostsButton.getFirst().click();
                        LogUtility.logInfo("Clicking show more follows button");
                    }catch (StaleElementReferenceException | NoSuchElementException ex){
                        //Show button probably disappeared because of scroll.
                    }
                    follows = driver.findElements(By.xpath(USER_XPATH));
                    try{
                        WaitHelper.waitForElementExistence(By.xpath(SHOW_MORE_FOLLOWS_XPATH),Duration.ofSeconds(5),driver);
                    }catch(TimeoutException ex){

                    }
                }
            }

            List<WebElement> userNames = driver.findElements(By.xpath("//div[@class='community-user-list__user-username']"));
            for(WebElement userName: userNames){
                foundUrls.add("https://roosterteeth.com/g/user/" + userName.getText().replace(" ","%20"));
            }

            //Additional wait just in case for archiving extension
            try{
                Thread.sleep(Duration.ofSeconds(20));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    String postsURL;
    public void archivePosts(){
        postsURL = driver.getCurrentUrl();

        //Removes a potential blocking element
        ((JavascriptExecutor)driver).executeScript("document.querySelectorAll(\".onetrust-pc-dark-filter\").forEach(e => e.remove())\n");

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
            foundUrls.add("https://roosterteeth.com/g/user/" + poster.getText().replace(" ","%20"));
        }
        List<WebElement> repostUserNames = driver.findElements(By.xpath("//span[@class='community-feed__marker-username']"));
        for(WebElement reposter:repostUserNames){
            foundUrls.add("https://roosterteeth.com/g/user/" + reposter.getText().replace(" ","%20"));
        }


        //Iterate over list opening comments
        LogUtility.logInfo("Clicking on comment buttons");
        List<WebElement> commentButtons = driver.findElements(By.xpath("//div[@class='community-feed__option community-feed__option--comment ']"));
        for (int i = 0; i < commentButtons.size(); i++) {
            WebElement commentButton = commentButtons.get(i);
            Actions actions = new Actions(driver);
            actions.scrollToElement(commentButton).build().perform();
            try{
                commentButton.click();
            }catch (ElementClickInterceptedException e){
                LogUtility.logInfo("Element click interception. Trying to remove one trust filter");
                ((JavascriptExecutor)driver).executeScript("document.querySelectorAll(\".onetrust-pc-dark-filter\").forEach(e => e.remove())\n");
                commentButtons = driver.findElements(By.xpath("//div[@class='community-feed__option community-feed__option--comment ']"));
                commentButton = commentButtons.get(i);
                commentButton.click();
            }

            WebElement commentCountElement = driver.findElement(By.xpath("(//div[contains(@class,'community-feed__option--comment')]//span[@class='community-feed__option-text'])[" + (i+1) +"]"));
            int commentCount;
            try{
              commentCount = Integer.parseInt(commentCountElement.getText().replace(" ",""));
            }catch (NumberFormatException e){
                commentCount =0;
            }
            LogUtility.logInfo("Comment count: " + commentCount);
            archivePostComments(commentCount);
        }

        //Additional wait just in case for archiving extension
        try{
            Thread.sleep(Duration.ofSeconds(20));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtility.logInfo("Finished archiving posts");
    }

    private static final String COMMENT_MODAL_XPATH = "//div[contains(@class,'modal-post-details')]";
    private static final String COMMENT_XPATH = "//article[@class='comment row ']";
    public void archivePostComments(int commentCount){
        String windowHandle = driver.getWindowHandle();
        WaitHelper.waitForElementExistence(By.xpath(COMMENT_MODAL_XPATH),Duration.ofSeconds(5),driver);
        WaitHelper.waitForElementVisible(By.xpath(COMMENT_MODAL_XPATH),Duration.ofSeconds(5),driver);
        if(commentCount != 0){
            //Click show more for comments
            List<WebElement> comments = driver.findElements(By.xpath(COMMENT_XPATH));
            if(comments.size() >= 20){
                boolean testing = false;
                if(testing){
                    throw new NoSuchElementException();
                }
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
                LogUtility.logInfo("Comment count does not match found comment size of: " + comments.size());
                List<WebElement> showReplies = driver.findElements(By.xpath("//button[@class='comment-replies-toggle']"));
                for (int i = 0; i < showReplies.size(); i++) {
                    WebElement showReplyButton = showReplies.get(i);
                    Actions actions = new Actions(driver);
                    try {
                        ScrollerUtility.scrollIntoMiddle(showReplyButton,driver);
                        showReplyButton.click();
                        LogUtility.logInfo("Clicking show more replies button");
                        if(driver.getWindowHandles().size() > 2){
                            LogUtility.logInfo("New tab opened!");
                            driver.switchTo().window((String) driver.getWindowHandles().toArray()[2]);
                            driver.close();
                            driver.switchTo().window(windowHandle);
                            showReplyButton.click();
                        }
                    } catch (StaleElementReferenceException e) {
                        showReplies = driver.findElements(By.xpath("//button[@class='comment-replies-toggle']"));
                        i--;
                    }catch (ElementClickInterceptedException e){
                        LogUtility.logInfo("Element click Interception!");
                        actions.scrollToElement(showReplyButton).build().perform();
                        showReplyButton.click();
                    }
                }
            }
            List<WebElement> commenters = driver.findElements(By.xpath("//h6/a[not(contains(@class,'comment__timeago '))]"));
            for (int i = 0; i < commenters.size(); i++) {
                WebElement commenter = commenters.get(i);
                try{
                    foundUrls.add(commenter.getAttribute("href"));
                }catch (StaleElementReferenceException e){
                    commenters = driver.findElements(By.xpath("//h6/a[not(contains(@class,'comment__timeago '))]"));
                    try{
                        foundUrls.add(commenters.get(i).getAttribute("href"));
                    }catch (IndexOutOfBoundsException indexOutOfBoundsException){
                        commenters = driver.findElements(By.xpath("//h6/a[not(contains(@class,'comment__timeago '))]"));
                        i = 0;
                    }
                } catch (IndexOutOfBoundsException e){
                    commenters = driver.findElements(By.xpath("//h6/a[not(contains(@class,'comment__timeago '))]"));
                    i = 0;
                }
            }
        }

        //Close out comment modal
        if(driver.getWindowHandles().size() > 2){
            LogUtility.logInfo("New tab opened!");
            driver.switchTo().window((String) driver.getWindowHandles().toArray()[2]);
            driver.close();
            driver.switchTo().window(windowHandle);
        }
        WebElement closeButton = driver.findElement(By.xpath("//span[contains(@class,'modal-close')]"));
        Actions actions = new Actions(driver);
        actions.scrollToElement(closeButton).click().build().perform();
        try{
            WaitHelper.waitForElementNotExist(By.xpath(COMMENT_MODAL_XPATH),Duration.ofSeconds(5),driver);
        }catch (TimeoutException e){
            e.printStackTrace();
        }

    }

    @Override
    public Set<String> getFoundUnarchivedURLS() {
        HashSet<String> uniqueFoundUrls = new HashSet<>();
        for(String url: foundUrls){
            boolean isPoster;
            if(url.charAt(url.length()-1) == '/'){
                isPoster = url.equals(postsURL);
            } else{
                isPoster = (url + "/").equals(postsURL);
            }
            if(!excludedURLS.contains(url) && !isPoster){
                uniqueFoundUrls.add(url);
            }
        }
        return uniqueFoundUrls;
    }

}
