package com.roosterteeth.worker;

import java.util.Set;

/**
 * Interface for an archive worker
 */
public interface IArchiveWorker {

    public Set<String> getArchivedURLS();

    public Set<String> getFoundUnarchivedURLS();


}
