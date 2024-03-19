package com.roosterteeth.utility;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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
}
