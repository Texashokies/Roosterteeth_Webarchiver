package com.roosterteeth.utility;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class WaitHelper {

    private WaitHelper(){
        throw new IllegalStateException("Utility class!");
    }

    /**
     * Waits for an element to exist based on by.
     * @param by The way to find the element
     * @param timeout Duration until the wait times out
     * @param driver The web driver to wait with.
     */
    public static void waitForElementExistence(By by, Duration timeout, WebDriver driver){
        WebDriverWait wait = new WebDriverWait(driver,timeout);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.until((driver1 -> driver1.findElement(by)));
    }

    /**
     * Waits for element to no longer exist based on by.
     * @param by The way to find the element
     * @param timeout Duration until the wait times out
     * @param driver The web driver to wait with
     */
    public static void waitForElementNotExist(By by,Duration timeout, WebDriver driver){
        WebDriverWait wait = new WebDriverWait(driver,timeout);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.until(driver1 -> driver1.findElements(by).isEmpty());
    }

    /**
     * Waits for either of the elements specified to exist.
     * @param firsyBy The first element to check for existing
     * @param secondBy The second element to check for existing
     * @param timeoutDuration Duration until the wait times out
     * @param driver The web driver to wait on.
     */
    public static void waitForEitherElementToExist(By firsyBy,By secondBy,Duration timeoutDuration,WebDriver driver){
        WebDriverWait wait = new WebDriverWait(driver,timeoutDuration);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.until((driver1 -> !driver1.findElements(firsyBy).isEmpty() || !driver1.findElements(secondBy).isEmpty()));
    }

    /**
     * Waits until the web driver has found more elements based on the given by.
     * @param by The way to find the elements
     * @param timeoutDuration Duration until the wait times out
     * @param previousSize The number that the number of elements should be greater than
     * @param driver The web driver to wait on
     */
    public static void waitForMoreElements(By by,Duration timeoutDuration,int previousSize, WebDriver driver){
        WebDriverWait wait = new WebDriverWait(driver,timeoutDuration);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.until((driver1 -> driver1.findElements(by).size() > previousSize));
    }

    public static void waitForElementCountToBe(By by,Duration timeoutDuration,int targetSize,WebDriver driver){
        WebDriverWait wait = new WebDriverWait(driver,timeoutDuration);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.until((driver1 -> driver1.findElements(by).size() == targetSize));
    }

    /**
     * Waits for the page to be in ready state
     * @param timeout Duration until the wait times out
     * @param driver The web driver to wait on
     */
    public static void waitForPageReady(Duration timeout,WebDriver driver){
        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.withTimeout(timeout);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.ignoring(NoSuchElementException.class);

        wait.until( webdriver -> ((JavascriptExecutor)webdriver).executeScript("return document.readyState").equals("complete"));
    }

    /**
     * Waits for an element to be visible
     * @param by The way to find the element
     * @param timeout Duration until the wait times out
     * @param driver The web driver to wait on
     */
    public static void waitForElementVisible(By by,Duration timeout,WebDriver driver){
        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.withTimeout(timeout);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.ignoring(NoSuchElementException.class);
        wait.until(webDrive->driver.findElement(by).isDisplayed());
    }

    /**
     * Waits for an element to not be visible
     * @param by The way to find the element
     * @param timeout Duration until the wait times out
     * @param driver The web driver to wait on
     */
    public static void waitForElementInvisible(By by,Duration timeout,WebDriver driver){
        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.withTimeout(timeout);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.ignoring(NoSuchElementException.class);
        wait.until(webDrive->!driver.findElement(by).isDisplayed());
    }

    /**
     * Waits for the page to scroll away from given scroll position
     * @param timeout Duration until the wait times out
     * @param driver The web driver to wait on
     * @param preScrollPosition The position that the page should no longer be scrolled at.
     */
    public static void waitForPageToScroll(Duration timeout,WebDriver driver,Long preScrollPosition){
        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.withTimeout(timeout);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.until(webDrive-> !Objects.equals(((JavascriptExecutor) driver).executeScript("return document.documentElement.scrollTop;"), preScrollPosition));
    }

    /**
     * Waits for a file to download
     * @param absoluteFilePath The absolute path to the file.
     * @param timeout Duration until the wait times out
     */
    public static void waitForFileToDownload(String absoluteFilePath,Duration timeout){
        File downloadedFile = new File(absoluteFilePath);

        FluentWait<File> wait = new FluentWait<>(downloadedFile);
        wait.pollingEvery(Duration.ofSeconds(5));
        wait.withTimeout(timeout);
        wait.ignoring(IOException.class);
        wait.withMessage("Archive file is not downloading in archive folder!");
        wait.until( (file -> file.exists() && file.canRead()));
    }

    /**
     * Waits for the currently active tab to be the given url.
     * @param url The url the tab should be
     * @param timeout The duration until wait times out
     * @param driver The webdriver
     * @param ignoreQuery If the check should ignore query params
     */
    public static void waitForUrlToBe(String url,Duration timeout,WebDriver driver, boolean ignoreQuery){
        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.withTimeout(timeout);
        wait.until(webdriver->{
            if(ignoreQuery){
                return StringUtils.substringBefore(driver.getCurrentUrl(),"?").equals(url);
            } else {
                return driver.getCurrentUrl().equals(url);
            }
        });
    }

    /**
     * Waits for the element found to have the given class
     * @param by The way to find the element
     * @param classToWaitFor The class the element should have
     * @param timeout The duration until wait times out
     * @param driver The webdriver
     */
    public static void waitForElementToHaveClass(By by,String classToWaitFor,Duration timeout,WebDriver driver){
        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.withTimeout(timeout);
        wait.until(webdriver->driver.findElement(by).getAttribute("class").contains(classToWaitFor));
    }

    /**
     * Waits for the elements given attribute to not match previous value
     * @param by The way to find the element
     * @param attribute The attribute to check
     * @param previousValue The previous value of the attribute it should change from
     * @param timeout The duration until wait times out
     * @param driver The webdriver
     */
    public static void waitForElementAttributeToChange(By by,String attribute,String previousValue,Duration timeout,WebDriver driver){
        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.withTimeout(timeout);
        wait.until(webdriver->!driver.findElement(by).getAttribute(attribute).equals(previousValue));
    }

    /**
     * Waits for a new window to be opened.
     * @param timeout The duration until wait times out
     * @param driver The webdriver
     * @param previousSize The number of windows that were open before the new window opens
     */
    public static void waitForNewWindowToOpen(Duration timeout,WebDriver driver,int previousSize){
        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.pollingEvery((Duration.ofMillis(250)));
        wait.withTimeout(timeout);
        wait.until(webdriver->driver.getWindowHandles().size()>previousSize);
    }

    public static void waitForElementToHaveTextInShadowDom(List<By> by, Duration duration, WebDriver driver, String text) {
        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.pollingEvery(Duration.ofMillis(250));
        wait.withTimeout(duration);
        wait.until(webdriver->{

            SearchContext searchContext = driver.findElement(by.getFirst());
            for(int i = 1; i<by.size()-1;i++){
                searchContext = searchContext.findElement(by.get(i));
            }

            return searchContext.findElement(by.getLast()).getText().equals(text);
        });
    }
}
