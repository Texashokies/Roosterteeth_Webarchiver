package com.roosterteeth.pages;

import java.util.Set;

public interface IRoosterteethPage {

    void archivePage();

    Set<String> getFoundUnarchivedURLS();

}
