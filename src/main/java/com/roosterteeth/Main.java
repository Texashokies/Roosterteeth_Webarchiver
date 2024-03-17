package com.roosterteeth;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        ChromeOptions options = new ChromeOptions();
        File recorderExtension = new File("src/main/resources/extensions/Webrecorder-ArchiveWeb-page.crx");
        options.addExtensions(recorderExtension);
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        WebDriver driver = new ChromeDriver(options);

        //Install web recorder extension

        driver.get("chrome-extension://fpeoodllldobpkbkabpblcfaogecpndd/replay/index.html");
        driver.manage().window().maximize();

        WebElement archivePage = driver.findElement(By.tagName("archive-web-page-app"));
        SearchContext shadowRoot = archivePage.getShadowRoot();

        shadowRoot.findElement(By.cssSelector("section > div > div > div > button:nth-child(1)")).click();
        shadowRoot.findElement(By.cssSelector("#new-title")).submit(); //This will create a blank names archive.

        SearchContext archiveShadowRoot = shadowRoot.findElement(By.cssSelector("wr-rec-coll-index")).getShadowRoot().findElement(By.cssSelector("wr-rec-coll-info")).getShadowRoot();

        //Load the starting json.

        //Download archiver
        archiveShadowRoot.findElement(By.cssSelector("div > div:nth-child(4) > div > a")).click();

        //TODO Wait until file exits/download stops
//        final String archiveName = "webarchive.wacz";
//        File downloadedArchive = new File(System.getProperty("user.dir") + "/" +archiveName);


        driver.quit();
    }
}