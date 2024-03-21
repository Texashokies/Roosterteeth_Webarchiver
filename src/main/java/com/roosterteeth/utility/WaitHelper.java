package com.roosterteeth.utility;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitHelper {

    private WaitHelper(){
        throw new IllegalStateException("Utility class!");
    }

    public static void waitForElementExistence(By by, long timeoutInSeconds, WebDriver driver){
        WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(timeoutInSeconds));
        wait.pollingEvery(Duration.ofMillis(250));
        wait.until((driver1 -> driver1.findElement(by)));
    }

    public static void waitForElementNotExist(By by,long timeoutInSeconds, WebDriver driver){
        WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(timeoutInSeconds));
        wait.pollingEvery(Duration.ofMillis(250));
        wait.until(driver1 -> driver1.findElements(by).isEmpty());
    }


    public static void waitForPageReady(long timeoutInSeconds,WebDriver driver){
        FluentWait wait = new FluentWait(driver);
        wait.withTimeout(Duration.ofSeconds(timeoutInSeconds));
        wait.pollingEvery(Duration.ofMillis(250));
        wait.ignoring(NoSuchElementException.class);

        wait.until( webdriver -> ((JavascriptExecutor)webdriver).executeScript("return document.readyState").equals("complete"));
    }

    public static void waitForElementVisible(By by,long timeoutInSeconds,WebDriver driver){
        FluentWait wait = new FluentWait(driver);
        wait.withTimeout(Duration.ofSeconds(timeoutInSeconds));
        wait.pollingEvery(Duration.ofMillis(250));
        wait.ignoring(NoSuchElementException.class);
        wait.until(webDrive->driver.findElement(by).isDisplayed());
    }

}
