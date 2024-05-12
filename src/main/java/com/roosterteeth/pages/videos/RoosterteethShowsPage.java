package com.roosterteeth.pages.videos;

import com.roosterteeth.pages.RoosterteethPage;
import com.roosterteeth.utility.ScrollerUtility;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoosterteethShowsPage extends RoosterteethPage {
    public RoosterteethShowsPage(String url, WebDriver driver, HashSet<String> excludedURLS) {
        super(url, driver, excludedURLS);
    }

    /**
     * Opens page and waits 30 seconds for archiving.
     */
    @Override
    public void archivePage() {
        driver.get(url);

        List<WebElement> shows = driver.findElements(By.xpath("//a[@class='card-link']"));
        while(shows.size() < 423){
            ScrollerUtility.scrollToPageBottom(driver);
            shows = driver.findElements(By.xpath("//a[@class='card-link']"));
        }

        try{
            Thread.sleep(Duration.ofMinutes(1));
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getFoundUnarchivedURLS() {
        HashSet<String> showURLS = new HashSet<>();
        List<WebElement> shows = driver.findElements(By.xpath("//a[@class='card-link']"));

        for(WebElement show: shows){
            showURLS.add(show.getAttribute("href"));
        }

        return showURLS;
    }


}
