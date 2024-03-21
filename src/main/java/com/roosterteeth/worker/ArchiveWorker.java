package com.roosterteeth.worker;

import com.roosterteeth.exceptions.InvalidURLException;
import com.roosterteeth.pages.RoosterteethPage;
import com.roosterteeth.pages.RoosterteethPageFactory;
import com.roosterteeth.utility.LogUtility;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * A class for an archive worker, that owns an instance of chrome driver.
 */
public class ArchiveWorker implements Runnable,IArchiveWorker{

    private final HashSet<String> completedURLS;
    private final HashSet<String> foundURLS;

    private final HashSet<String> urlsToArchive;

    private final HashSet<String> excludedURLS;

    private WebDriver driver;

    private final int workerID;

    private String archiveName;

    private static final String EXTENSIONINDEX = "chrome-extension://fpeoodllldobpkbkabpblcfaogecpndd/replay/index.html";
    private static final String ARCHIVEPAGESELECTOR = "archive-web-page-app";

    /**
     * Creates an archive worker with given set of urls to archive, to not archive, and it's id
     * @param urlsToArchive The set of urls the archive worker should archive.
     * @param excludedURLS Set of urls the archive worker should not archive or add to lists for archiving
     * @param workerID The unique ID of the archive worker
     * @param archiveName What the created archive should be named.
     */
    public ArchiveWorker(Set<String> urlsToArchive, Set<String> excludedURLS,int workerID,String archiveName){
        this.urlsToArchive = (HashSet<String>) urlsToArchive;
        this.excludedURLS = (HashSet<String>) excludedURLS;
        this.workerID = workerID;
        foundURLS = new HashSet<>();
        completedURLS = new HashSet<>();
        this.archiveName = archiveName + "_worker"+workerID;
    }

    @Override
    public HashSet<String> getArchivedURLS() {
        return completedURLS;
    }

    @Override
    public HashSet<String> getFoundUnarchivedURLS() {
        return foundURLS;
    }

    /**
     * Starts the chrome driver instance and creates the archive.
     */
    private void startArchive(){
        ChromeOptions options = new ChromeOptions();
        File recorderExtension = new File("src/main/resources/extensions/Webrecorder-ArchiveWeb-page.crx");
        options.addExtensions(recorderExtension);
        //TODO set download location to directory
        driver = new ChromeDriver(options);

        //Install web recorder extension
        driver.get(EXTENSIONINDEX);
        driver.manage().window().maximize();

        //Create new archive
        WebElement archivePage = driver.findElement(By.tagName(ARCHIVEPAGESELECTOR));
        SearchContext shadowRoot = archivePage.getShadowRoot();

        shadowRoot.findElement(By.cssSelector("section > div > div > div > button:nth-child(1)")).click();
        WebElement archiveNameInput = shadowRoot.findElement(By.cssSelector("#new-title"));
        ((JavascriptExecutor)driver).executeScript("arguments[0].value = arguments[1]",archiveNameInput,archiveName);
        WebElement createButton = shadowRoot.findElement(By.cssSelector("wr-modal > form > div > div > button"));
        ((JavascriptExecutor)driver).executeScript("arguments[0].click()",createButton);
    }

    /**
     * Creates a chrome driver instance and new archive. Archives all provided urls.
     */
    @Override
    public void run() {
        LogUtility.logInfo(String.format("Worker %d started", workerID));
        startArchive();

        String archiveIndexHandle = driver.getWindowHandle();
        for(String url: urlsToArchive){
            driver.switchTo().window(archiveIndexHandle);
            WebElement archivePage = driver.findElement(By.tagName(ARCHIVEPAGESELECTOR));
            SearchContext shadowRoot = archivePage.getShadowRoot();

            WebElement startRecordingButton = shadowRoot.findElement(By.cssSelector("section > div > div > div > button:nth-child(3)"));
            startRecordingButton.click();

            //TODO open page to archive and run archival logic
            WebElement urlInput = shadowRoot.findElement(By.cssSelector("#url"));
            ((JavascriptExecutor)driver).executeScript("arguments[0].value = arguments[1]",urlInput,url);
            WebElement goButton=  shadowRoot.findElement(By.cssSelector("wr-modal > form > div > div > button"));
            ((JavascriptExecutor)driver).executeScript("arguments[0].click()",goButton);
            //Switch to recently opened handle
            for(String windowHandle : driver.getWindowHandles()){
                if(!archiveIndexHandle.equals(windowHandle)){
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            try {
                RoosterteethPage page = RoosterteethPageFactory.getRoosterteethPageFromURL(url,driver);
                page.archivePage();
            } catch (InvalidURLException e) {
                e.printStackTrace();
            }
            //Close page
            driver.close();
        }
        driver.switchTo().window(archiveIndexHandle);
        endArchiving();
    }



    /**
     * Downloads the created archive and stops the chrome driver instance.
     */
    private void endArchiving(){
        driver.get(EXTENSIONINDEX);
        WebElement archivePage = driver.findElement(By.tagName(ARCHIVEPAGESELECTOR));
        SearchContext shadowRoot = archivePage.getShadowRoot();
        SearchContext archiveShadowRoot = shadowRoot.findElement(By.cssSelector("wr-rec-coll-index")).getShadowRoot().findElement(By.cssSelector("wr-rec-coll-info")).getShadowRoot();
        archiveShadowRoot.findElement(By.cssSelector("div > div:nth-child(4) > div > a")).click();
        //TODO add a wait for download to complete.
        driver.quit();
    }



}
