package com.roosterteeth.pages.videos;

import com.roosterteeth.pages.RoosterteethPage;
import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.utility.ScrollerUtility;
import com.roosterteeth.utility.WaitHelper;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoosterteethShowPage extends RoosterteethPage {
    public RoosterteethShowPage(String url, WebDriver driver, HashSet<String> excludedURLS) {
        super(url, driver, excludedURLS);
    }

    private HashSet<String> foundUrls = new HashSet<>();

    private static final String EPISODE_XPATH = "//section[@class='carousel-container']//div[@class='episode-card']/div/div/a";
    private static final String BONUS_XPATH = "//section[@class='show-carousels']//div[@class='episode-card']/div/div/a";


    @Override
    public void archivePage() {
        driver.get(url);


        WaitHelper.waitForElementExistence(By.xpath("//ul[@class='rt-dropdown-list']"),Duration.ofSeconds(5),driver);
        List<WebElement> seasons = driver.findElements(By.xpath("//ul[@class='rt-dropdown-list']/li"));

        int seasonIndex = 0;
        do{
            WebElement seasonDropdown = driver.findElement(By.xpath("//div[@class='rt-dropdown-list-button']"));
            Actions actions = new Actions(driver);
            actions.scrollToElement(seasonDropdown).click(seasonDropdown).build().perform();
            WaitHelper.waitForElementVisible(By.xpath("//ul[@class='rt-dropdown-list']/li"),Duration.ofSeconds(5),driver);
            actions.moveToElement(seasons.get(seasonIndex)).click(seasons.get(seasonIndex)).build().perform();

            try{
                Thread.sleep(Duration.ofSeconds(2));
            }catch (InterruptedException e){
                e.printStackTrace();
            }


            seasonDropdown = driver.findElement(By.xpath("//div[@class='rt-dropdown-list-button']"));
            LogUtility.logInfo("Season: " + seasonDropdown.getText());

            ScrollerUtility.scrollToBottomOfElement(driver.findElement(By.xpath("//section[@class='carousel-container']")),driver);
            List<WebElement> episodes = driver.findElements(By.xpath(EPISODE_XPATH));
            LogUtility.logInfo("Episodes: " + episodes.size());

            if(episodes.size() >= 24){
                List<WebElement> showMoreButton = driver.findElements(By.xpath("//div[contains(@class,'show-more')]"));
                if(!showMoreButton.isEmpty()){
                    showMoreButton.getFirst().click();
                    WaitHelper.waitForMoreElements(By.xpath(EPISODE_XPATH),Duration.ofSeconds(30),episodes.size(),driver);
                }
            }

            episodes = driver.findElements(By.xpath(EPISODE_XPATH));
            for(WebElement episode: episodes){
                foundUrls.add(episode.getAttribute("href"));
            }


            seasonIndex++;
        }while (seasonIndex < seasons.size());

        //TODO Get bonus features

        try{
            Thread.sleep(Duration.ofMinutes(1));
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getFoundUnarchivedURLS() {
        HashSet<String> uniqueFoundUrls = new HashSet<>();
        for(String url: foundUrls){
            if(!excludedURLS.contains(url)){
                uniqueFoundUrls.add(url);
            }
        }
        return uniqueFoundUrls;
    }
}
