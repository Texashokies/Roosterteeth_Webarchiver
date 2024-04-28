package com.roosterteeth.pages.community;

import com.roosterteeth.utility.ScrollerUtility;
import com.roosterteeth.utility.WaitHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.util.HashSet;

public class RoosterTeethUserPage extends RoosterteethCommunityPage{
    public RoosterTeethUserPage(String url, WebDriver driver, HashSet<String> excludedURLS) {
        super(url, driver, excludedURLS);
    }

    /**
     * Opens all community posts into comments modal, and clicks show more for posts. Opens following and followers tab
     */
    @Override
    public void archivePage() {
        //Click show more for posts
        super.archivePage();
        ScrollerUtility.scrollToTopOfElement(driver.findElement(By.xpath("//div[@class='rt-halves__half half--right']")),driver);
        driver.findElement(By.xpath("//div[contains(@class,'rt-profile-tabs__tab wdio-tab-following ')]")).click();
        WaitHelper.waitForPageReady(Duration.ofSeconds(5),driver);
        archiveFollows();

        ScrollerUtility.scrollToTopOfElement(driver.findElement(By.xpath("//div[@class='rt-halves__half half--right']")),driver);
        driver.findElement(By.xpath("//div[contains(@class,'rt-profile-tabs__tab wdio-tab-followers ')]")).click();
        WaitHelper.waitForPageReady(Duration.ofSeconds(5),driver);
        archiveFollows();

        ScrollerUtility.scrollToTopOfElement(driver.findElement(By.xpath("//div[@class='rt-halves__half half--right']")),driver);
        driver.findElement(By.xpath("//div[contains(@class,'rt-profile-tabs__tab wdio-tab-groups ')]")).click();
        WaitHelper.waitForPageReady(Duration.ofSeconds(5),driver);
    }
}
