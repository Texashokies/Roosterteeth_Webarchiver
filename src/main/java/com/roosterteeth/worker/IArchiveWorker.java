package com.roosterteeth.worker;

import java.util.HashSet;
import java.util.Set;

/**
 * Interface for an archive worker
 */
public interface IArchiveWorker {

    Set<String> getArchivedURLS();

    Set<String> getFailedURLS();

    Set<String> getFoundUnarchivedURLS();

    HashSet<String> getUrlsToArchive();
}
