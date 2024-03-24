package com.roosterteeth.utility;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ScrollerUtility{
    private ScrollerUtility(){
        throw new IllegalStateException("Utility Class!");
    }

    public static void scrollToBottomOfElement(WebElement element, WebDriver driver){
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollTo(0, arguments[0].scrollHeight)",element);
    }

    public static void scrollToTopOfElement(WebElement element, WebDriver driver){
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollTo(0, 0)",element);
    }

    public static void scrollToPageBottom(WebDriver driver) {
        ((JavascriptExecutor) driver)
                .executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }
}
