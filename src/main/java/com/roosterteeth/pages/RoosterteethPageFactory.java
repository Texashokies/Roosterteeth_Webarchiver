package com.roosterteeth.pages;

import com.roosterteeth.exceptions.InvalidURLException;
import com.roosterteeth.pages.community.RoosteerTeethGroupPage;
import com.roosterteeth.pages.community.RoosterTeethGroupsPage;
import com.roosterteeth.pages.community.RoosterTeethUserPage;
import com.roosterteeth.pages.community.RoosterteethCommunityPage;
import com.roosterteeth.pages.rtstore.StoreCollectionPage;
import com.roosterteeth.pages.rtstore.StoreProductPage;
import com.roosterteeth.pages.rtstoreuk.UKStoreCollectionPage;
import com.roosterteeth.pages.rtstoreuk.UKStoreProductPage;
import com.roosterteeth.pages.videos.RoosterteethShowPage;
import com.roosterteeth.pages.videos.RoosterteethShowsPage;
import org.openqa.selenium.WebDriver;

import java.util.HashSet;

public class RoosterteethPageFactory {

    private RoosterteethPageFactory(){
        throw new IllegalStateException("Utility class!");
    }

    public static RoosterteethPage getRoosterteethPageFromURL(String url, WebDriver driver, HashSet<String> excludedURLS) throws InvalidURLException {
        if(!(url.contains("roosterteeth.com") || url.contains("roosterteeth.co.uk"))){
            throw new InvalidURLException(String.format("Provided url %s is not on a Roosterteeth site", url));
        }

        if(url.contains("/g")){
            if (url.contains("/g/all")) {
                return new RoosterTeethGroupsPage(url,driver,excludedURLS);
            } else if(!url.contains("/user")){
                return new RoosteerTeethGroupPage(url,driver,excludedURLS);
            } else if(url.contains("/user")) {
                return new RoosterTeethUserPage(url,driver,excludedURLS);
            }
            //TODO distinguish between users and groups
            return new RoosterteethCommunityPage(url,driver,excludedURLS);
        }

        if(url.contains("store.roosterteeth.com")){
            if(url.contains("/collections") && !url.contains("/products")){
                return new StoreCollectionPage(url,driver,excludedURLS);
            } else if(url.contains("/products")){
                return new StoreProductPage(url,driver,excludedURLS);
            }
        } else if(url.contains("store.roosterteeth.co.uk")){
            if(url.contains("/collections") && !url.contains("/products")){
                return new UKStoreCollectionPage(url,driver,excludedURLS);
            } else if(url.contains("/products")){
                return new UKStoreProductPage(url,driver,excludedURLS);
            }
        }

        if(url.contains("/series")){
            if(!url.contains("/series/")){
                return new RoosterteethShowsPage(url,driver,excludedURLS);
            }else{
                return new RoosterteethShowPage(url,driver,excludedURLS);
            }
        }

        return new RoosterteethPage(url,driver,excludedURLS);
    }
}
