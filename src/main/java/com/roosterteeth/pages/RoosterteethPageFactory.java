package com.roosterteeth.pages;

import com.roosterteeth.exceptions.InvalidURLException;
import org.openqa.selenium.WebDriver;

public class RoosterteethPageFactory {

    private RoosterteethPageFactory(){
        throw new IllegalStateException("Utility class!");
    }

    public static RoosterteethPage getRoosterteethPageFromURL(String url, WebDriver driver) throws InvalidURLException {
        if(!(url.contains("roosterteeth.com") || url.contains("roosterteeth.co.uk"))){
            throw new InvalidURLException(String.format("Provided url %s is not on a Roosterteeth site", url));
        }

        if(url.contains("/g")){
            //TODO distinguish between users and groups
            return new RoosterteethCommunityPage(url,driver);
        }

        return new RoosterteethPage(url,driver);
    }
}
