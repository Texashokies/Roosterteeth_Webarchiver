package com.roosterteeth.hooks;

import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.worker.ArchiveWorker;

public class WorkerShutdownHook extends Thread {

    ArchiveWorker worker;
    final String archiveIndexHandle;

    public WorkerShutdownHook(ArchiveWorker worker, String archiveIndexHandle) {
        this.worker = worker;
        this.archiveIndexHandle = archiveIndexHandle;
    }
    public void run() {
        if(!worker.hasArchived()){
            LogUtility.logInfo("Worker shutting down...");
            LogUtility.logInfo("Worker completed: " + worker.getArchivedURLS().toString());
            worker.switchToHandle(archiveIndexHandle);
            worker.endArchiving();
        }
    }
}
