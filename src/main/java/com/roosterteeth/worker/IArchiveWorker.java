package com.roosterteeth.worker;

import java.util.Set;

/**
 * Interface for an archive worker
 */
public interface IArchiveWorker {

    Set<String> getArchivedURLS();

    Set<String> getFoundUnarchivedURLS();


}
