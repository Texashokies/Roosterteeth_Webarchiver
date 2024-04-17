package com.roosterteeth.pages.rtstoreuk;

import com.roosterteeth.pages.RoosterteethPage;
import org.openqa.selenium.WebDriver;

import java.util.HashSet;

public class UKStoreProductPage extends RoosterteethPage {
    public UKStoreProductPage(String url, WebDriver driver, HashSet<String> excludedURLS) {
        super(url, driver, excludedURLS);
    }
}
