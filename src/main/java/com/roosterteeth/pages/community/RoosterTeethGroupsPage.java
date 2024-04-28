package com.roosterteeth.pages.community;

import com.roosterteeth.pages.RoosterteethPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoosterTeethGroupsPage extends RoosterteethPage {
    public RoosterTeethGroupsPage(String url, WebDriver driver, HashSet<String> excludedURLS) {
        super(url, driver, excludedURLS);
    }

    @Override
    public Set<String> getFoundUnarchivedURLS() {
        HashSet<String> groupUrls = new HashSet<>();
        List<WebElement> groups = driver.findElements(By.xpath("//a[@class='community-card-vertical']"));

        for(WebElement group: groups){
            groupUrls.add(group.getAttribute("href"));
        }

        return groupUrls;
    }
}
