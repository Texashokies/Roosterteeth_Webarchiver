package com.roosterteeth.pages.community;

import com.roosterteeth.utility.ScrollerUtility;
import com.roosterteeth.utility.WaitHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.HashSet;

public class RoosteerTeethGroupPage extends RoosterteethCommunityPage{
    public RoosteerTeethGroupPage(String url, WebDriver driver, HashSet<String> excludedURLS) {
        super(url, driver, excludedURLS);
    }

    /**
     * Opens all community posts into comments modal, and clicks show more for posts. Opens member tab
     */
    @Override
    public void archivePage() {
        //Click show more for posts
        if(!driver.getCurrentUrl().equals(url)) {
            driver.get(url);
        }

        ScrollerUtility.scrollToTopOfElement(driver.findElement(By.xpath("//div[@class='rt-halves__half half--right']")),driver);
        driver.findElement(By.xpath("//div[contains(@class,'rt-profile-tabs__tab wdio-tab-members')]")).click();
        WaitHelper.waitForPageReady(Duration.ofSeconds(5),driver);
        WebElement membersCount = driver.findElement(By.xpath("(//div[@class='follows-item__count'])[1]"));
        archiveFollows(Integer.parseInt(membersCount.getText()));

        super.archivePage();
    }
}
