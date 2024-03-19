package com.roosterteeth.pages;

import org.openqa.selenium.WebDriver;

public class RoosterteethPage implements IRoosterteethPage{

    protected final WebDriver driver;
    protected final String url;

    public RoosterteethPage(String url,WebDriver driver){
        this.url = url;
        this.driver = driver;
    }

    /**
     * Opens page
     */
    @Override
    public void archivePage() {
        driver.get(url);
    }
}
