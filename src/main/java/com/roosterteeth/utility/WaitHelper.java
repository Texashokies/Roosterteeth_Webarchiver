package com.roosterteeth.utility;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
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

}
