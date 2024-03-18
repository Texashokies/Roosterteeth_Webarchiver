package com.roosterteeth.worker;

import com.roosterteeth.utility.LogUtility;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ArchiveWorker implements Runnable,IArchiveWorker{

    private final HashSet<String> completedURLS;
    private final HashSet<String> foundURLS;

    private final HashSet<String> urlsToArchive;

    private final HashSet<String> excludedURLS;

    private WebDriver driver;

    private final int workerID;

    private static final String EXTENSIONINDEX = "chrome-extension://fpeoodllldobpkbkabpblcfaogecpndd/replay/index.html";

    public ArchiveWorker(Set<String> urlsToArchive, Set<String> excludedURLS,int workerID){
        this.urlsToArchive = (HashSet<String>) urlsToArchive;
        this.excludedURLS = (HashSet<String>) excludedURLS;
        this.workerID = workerID;
        foundURLS = new HashSet<>();
        completedURLS = new HashSet<>();
    }

    @Override
    public HashSet<String> getArchivedURLS() {
        return completedURLS;
    }

    @Override
    public HashSet<String> getFoundUnarchivedURLS() {
        return foundURLS;
    }

    @Override
    public void run() {
        LogUtility.logInfo(String.format("Worker %d started", workerID));
        startArchive();
        for(String url: urlsToArchive){
            driver.get(EXTENSIONINDEX);
            WebElement archivePage = driver.findElement(By.tagName("archive-web-page-app"));
            SearchContext shadowRoot = archivePage.getShadowRoot();

            WebElement startRecordingButton = shadowRoot.findElement(By.cssSelector("section > div > div > div > button:nth-child(3)"));
            startRecordingButton.click();

            shadowRoot = shadowRoot.findElement(By.cssSelector("wr-modal")).getShadowRoot();

            //TODO open page to archive and run archival logic
            shadowRoot.findElement(By.cssSelector("#url")).sendKeys(url);
            shadowRoot.findElement(By.cssSelector("wr-modal > form > div > div > button")).click();
        }
        endArchiving();
    }

    /**
     * Starts the chrome driver instance and creates the archive.
     */
    private void startArchive(){
        ChromeOptions options = new ChromeOptions();
        File recorderExtension = new File("src/main/resources/extensions/Webrecorder-ArchiveWeb-page.crx");
        options.addExtensions(recorderExtension);
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        driver = new ChromeDriver(options);

        //Install web recorder extension
        driver.get(EXTENSIONINDEX);
        driver.manage().window().maximize();

        //Create new archive
        WebElement archivePage = driver.findElement(By.tagName("archive-web-page-app"));
        SearchContext shadowRoot = archivePage.getShadowRoot();

        shadowRoot.findElement(By.cssSelector("section > div > div > div > button:nth-child(1)")).click();
        shadowRoot.findElement(By.cssSelector("#new-title")).submit(); //This will create a blank names archive.

    }

    /**
     * Downloads the created archive and stops the chrome driver instance.
     */
    private void endArchiving(){
        driver.get(EXTENSIONINDEX);
        WebElement archivePage = driver.findElement(By.tagName("archive-web-page-app"));
        SearchContext shadowRoot = archivePage.getShadowRoot();
        SearchContext archiveShadowRoot = shadowRoot.findElement(By.cssSelector("wr-rec-coll-index")).getShadowRoot().findElement(By.cssSelector("wr-rec-coll-info")).getShadowRoot();
        archiveShadowRoot.findElement(By.cssSelector("div > div:nth-child(4) > div > a")).click();
        driver.quit();
    }



}
