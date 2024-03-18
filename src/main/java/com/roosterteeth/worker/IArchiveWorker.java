package com.roosterteeth.worker;

import java.util.Set;

public interface IArchiveWorker {

    public Set<String> getArchivedURLS();

    public Set<String> getFoundUnarchivedURLS();


}
