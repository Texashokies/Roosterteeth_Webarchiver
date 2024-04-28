package com.roosterteeth.worker;

import com.roosterteeth.exceptions.InvalidURLException;
import com.roosterteeth.pages.RoosterteethPage;
import com.roosterteeth.pages.RoosterteethPageFactory;
import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.utility.WaitHelper;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.ClosedChannelException;
import java.time.Duration;
import java.util.HashMap;
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

    private String gridPath;

    private static final String EXTENSIONINDEX = "chrome-extension://fpeoodllldobpkbkabpblcfaogecpndd/replay/index.html";
    private static final String ARCHIVEPAGESELECTOR = "archive-web-page-app";

    /**
     * Creates an archive worker with given set of urls to archive, to not archive, and it's id
     * @param urlsToArchive The set of urls the archive worker should archive.
     * @param excludedURLS Set of urls the archive worker should not archive or add to lists for archiving
     * @param workerID The unique ID of the archive worker
     * @param archiveName What the created archive should be named.
     */
    public ArchiveWorker(Set<String> urlsToArchive, Set<String> excludedURLS,int workerID,String archiveName,String gridPath){
        this.urlsToArchive = (HashSet<String>) urlsToArchive;
        this.excludedURLS = (HashSet<String>) excludedURLS;
        this.workerID = workerID;
        foundURLS = new HashSet<>();
        completedURLS = new HashSet<>();
        this.archiveName = archiveName + "_worker"+workerID;
        this.gridPath = gridPath;
    }

    @Override
    public HashSet<String> getArchivedURLS() {
        return completedURLS;
    }

    @Override
    public HashSet<String> getFoundUnarchivedURLS() {
        return foundURLS;
    }

    final String archivesPath = System.getProperty("user.dir") + File.separatorChar + "archives";

    /**
     * Starts the chrome driver instance and creates the archive.
     */
    private void startArchive(){
        ChromeOptions options = new ChromeOptions();
        File recorderExtension = new File("src/main/resources/extensions/Webrecorder-ArchiveWeb-page.crx");
        options.addExtensions(recorderExtension);

        HashMap<String,Object> chromePrefs = new HashMap<>();

        chromePrefs.put("download.default_directory", archivesPath);
        options.setExperimentalOption("prefs",chromePrefs);
        if(gridPath != null){
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability(ChromeOptions.CAPABILITY,options);
            try {
                driver = new RemoteWebDriver( new URI(gridPath).toURL(),capabilities);
            } catch (URISyntaxException | SessionNotCreatedException | MalformedURLException e) {
                LogUtility.logError("Unable to start session in grid, starting in local!");
                driver = new ChromeDriver(options);
            }
        }else{
            driver = new ChromeDriver(options);
        }


        //Install web recorder extension
        driver.get(EXTENSIONINDEX);
        LogUtility.logInfo("Archive index Window handle: " + driver.getWindowHandle());
        driver.manage().window().maximize();
    }

    private void createNewArchive(){
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

        createNewArchive();

        WebElement archivePage = driver.findElement(By.tagName(ARCHIVEPAGESELECTOR));
        SearchContext shadowRoot = archivePage.getShadowRoot();

        SearchContext archiveShadowRoot = shadowRoot.findElement(By.cssSelector("wr-rec-coll-index")).getShadowRoot();
        while(archiveShadowRoot.findElements(By.cssSelector("div.coll-list")).isEmpty()){
            driver.navigate().refresh();
            createNewArchive();
            archivePage = driver.findElement(By.tagName(ARCHIVEPAGESELECTOR));
            shadowRoot = archivePage.getShadowRoot();
            archiveShadowRoot = shadowRoot.findElement(By.cssSelector("wr-rec-coll-index")).getShadowRoot();
        }

        WebElement startRecordingButton = shadowRoot.findElement(By.cssSelector("section > div > div > div > button:nth-child(3)"));
        try{
            startRecordingButton.click();
        }catch (ElementClickInterceptedException e){
            driver.navigate().refresh();
            archivePage = driver.findElement(By.tagName(ARCHIVEPAGESELECTOR));
            shadowRoot = archivePage.getShadowRoot();
            startRecordingButton = shadowRoot.findElement(By.cssSelector("section > div > div > div > button:nth-child(3)"));
            startRecordingButton.click();
        }

        String url = urlsToArchive.iterator().next();
        WebElement urlInput = shadowRoot.findElement(By.cssSelector("#url"));
        ((JavascriptExecutor)driver).executeScript("arguments[0].value = arguments[1]",urlInput,url);
        WebElement goButton=  shadowRoot.findElement(By.cssSelector("wr-modal > form > div > div > button"));
        ((JavascriptExecutor)driver).executeScript("arguments[0].click()",goButton);
        WaitHelper.waitForNewWindowToOpen(Duration.ofSeconds(10),driver,1);
        //Switch to recently opened handle
        String archiveIndexHandle = driver.getWindowHandle();
        for(String windowHandle : driver.getWindowHandles()){
            if(!archiveIndexHandle.equals(windowHandle)){
                driver.switchTo().window(windowHandle);
                try{
                    WaitHelper.waitForUrlToBe(url,Duration.ofSeconds(20),driver,true);
                }catch (TimeoutException e){
                    LogUtility.logInfo("Shutting down webdriver and trying again");
                    driver.quit();
                    run();
                    return;
                }
                break;
            }
        }

        int urlIndex = 1;
        for(String urlToArchive:urlsToArchive){
            LogUtility.logInfo(String.format("Starting to archive url %d of %d", urlIndex++,urlsToArchive.size()));
            try {
                RoosterteethPage page = RoosterteethPageFactory.getRoosterteethPageFromURL(urlToArchive,driver,excludedURLS);
                page.archivePage();
                foundURLS.addAll(page.getFoundUnarchivedURLS());
            } catch (InvalidURLException e) {
                e.printStackTrace();
            }
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
        //TODO may want to check that file doesn't already exists so proper name can be searched in wait.
        archiveShadowRoot.findElement(By.cssSelector("div > div:nth-child(4) > div > a")).click();
        WaitHelper.waitForFileToDownload( archivesPath + File.separator + archiveName + ".wacz", Duration.ofMinutes(10));
        driver.quit();
    }



}
