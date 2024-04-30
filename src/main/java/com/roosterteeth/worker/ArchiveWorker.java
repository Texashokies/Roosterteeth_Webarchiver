package com.roosterteeth.worker;

import com.roosterteeth.exceptions.InvalidURLException;
import com.roosterteeth.hooks.WorkerShutdownHook;
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A class for an archive worker, that owns an instance of chrome driver.
 */
public class ArchiveWorker implements Runnable,IArchiveWorker{

    private final HashSet<String> completedURLS;
    private final HashSet<String> failedURLS;
    private final HashSet<String> foundURLS;

    private final HashSet<String> urlsToArchive;

    private final HashSet<String> excludedURLS;

    private WebDriver driver;

    private final int workerID;

    private String archiveName;

    private int pass;

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
    public ArchiveWorker(Set<String> urlsToArchive, Set<String> excludedURLS,int workerID,String archiveName,int pass,String gridPath){
        this.urlsToArchive = (HashSet<String>) urlsToArchive;
        this.excludedURLS = (HashSet<String>) excludedURLS;
        this.workerID = workerID;
        foundURLS = new HashSet<>();
        completedURLS = new HashSet<>();
        this.archiveName = archiveName;
        this.pass = pass;
        this.gridPath = gridPath;
        failedURLS = new HashSet<>();
    }

    @Override
    public HashSet<String> getArchivedURLS() {
        return completedURLS;
    }

    @Override
    public Set<String> getFailedURLS() {
        return failedURLS;
    }

    @Override
    public HashSet<String> getFoundUnarchivedURLS() {
        return foundURLS;
    }

    @Override
    public HashSet<String> getUrlsToArchive() {
        return urlsToArchive;
    }

    /**
     * Starts the chrome driver instance and creates the archive.
     */
    private void startArchive(){
        ChromeOptions options = new ChromeOptions();
        File recorderExtension = new File("extensions/Webrecorder-ArchiveWeb-page.crx");
        options.addExtensions(recorderExtension);

        HashMap<String,Object> chromePrefs = new HashMap<>();

        final String archivesPath = System.getProperty("user.dir") + File.separatorChar + "archives" + File.separatorChar + archiveName;

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
        ((JavascriptExecutor)driver).executeScript("arguments[0].value = arguments[1]",archiveNameInput,archiveName + String.format("_pass_%d_", pass) + "_worker"+workerID);
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
        WorkerShutdownHook shutdownHook = new WorkerShutdownHook(this,archiveIndexHandle,archiveName);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        //Will continue until done with last url. Ends archiving.
        for(String urlToArchive:urlsToArchive){
            LogUtility.logInfo(String.format("Starting to archive url %d of %d", urlIndex++,urlsToArchive.size()));
            try {
                RoosterteethPage page = RoosterteethPageFactory.getRoosterteethPageFromURL(urlToArchive,driver,excludedURLS);
                try{
                    page.archivePage();
                }catch (WebDriverException e){
                    failedURLS.add(urlToArchive);
                    continue;
                }
                foundURLS.addAll(page.getFoundUnarchivedURLS());
                completedURLS.add(urlToArchive);
            } catch (InvalidURLException e) {
                e.printStackTrace();
            }
        }
        driver.switchTo().window(archiveIndexHandle);
        endArchiving();
    }

    public void switchToHandle(String handle){
        driver.switchTo().window(handle);
    }

    boolean hasArchived = false;
    public boolean hasArchived(){
        return hasArchived;
    }

    /**
     * Downloads the created archive and stops the chrome driver instance.
     */
    public void endArchiving(){
        driver.get(EXTENSIONINDEX);
        WebElement archivePage = driver.findElement(By.tagName(ARCHIVEPAGESELECTOR));
        SearchContext shadowRoot = archivePage.getShadowRoot();
        SearchContext archiveShadowRoot = shadowRoot.findElement(By.cssSelector("wr-rec-coll-index")).getShadowRoot().findElement(By.cssSelector("wr-rec-coll-info")).getShadowRoot();
        //TODO may want to check that file doesn't already exists so proper name can be searched in wait.
        archiveShadowRoot.findElement(By.cssSelector("div > div:nth-child(4) > div > a")).click();
        final String archivesPath = System.getProperty("user.dir") + File.separatorChar + "archives" + File.separatorChar + archiveName;
        final String archiveFileName = archiveName + String.format("_pass_%d_", pass) + "_worker"+workerID;
        try{
            WaitHelper.waitForFileToDownload( archivesPath + File.separator + archiveFileName + ".wacz", Duration.ofMinutes(10));
        }catch (TimeoutException e){
            LogUtility.logError("Failed to download file! For worker " + workerID);
            //Still need to quite driver.
        }
        driver.quit();
        hasArchived = true;
    }


    /**
     * Gets the urls this worker is excluded from archiving
     * @return The set of urls the worker is excluded from archiving
     */
    public HashSet<String> getExcludedURLS() {
        return excludedURLS;
    }

    /**
     * Gets the id of the worker
     * @return The worker ID
     */
    public int getID() {
        return workerID;
    }
}
