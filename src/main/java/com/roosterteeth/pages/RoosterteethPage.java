package com.roosterteeth.pages;

import org.openqa.selenium.WebDriver;

import java.util.HashSet;
import java.util.Set;

public class RoosterteethPage implements IRoosterteethPage{

    protected final WebDriver driver;
    protected final String url;

    protected HashSet<String> excludedURLS;

    public RoosterteethPage(String url,WebDriver driver,HashSet<String> excludedURLS){
        this.url = url;
        this.driver = driver;
        this.excludedURLS = excludedURLS;
    }

    /**
     * Opens page
     */
    @Override
    public void archivePage() {
        driver.get(url);
    }

    @Override
    public Set<String> getFoundUnarchivedURLS() {
        return null;
    }
}
