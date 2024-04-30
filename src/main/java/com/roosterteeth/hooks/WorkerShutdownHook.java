package com.roosterteeth.hooks;

import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.worker.ArchiveWorker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Shutdown hook for ArchiveWorkers. Will download archive, and create output.json of worker results.
 */
public class WorkerShutdownHook extends Thread {

    ArchiveWorker worker;
    final String archiveIndexHandle;
    final String archiveName;

    public WorkerShutdownHook(ArchiveWorker worker, String archiveIndexHandle,String archiveName) {
        this.worker = worker;
        this.archiveIndexHandle = archiveIndexHandle;
        this.archiveName = archiveName;
    }

    /**
     * Downloads the recorded archive. Creates an output.json following archive name scheme for worker results.
     */
    public void run() {
        LogUtility.logInfo("Worker has archived: " + worker.hasArchived());
        if(!worker.hasArchived()){
            LogUtility.logInfo("Worker shutting down...");
            worker.switchToHandle(archiveIndexHandle);
            worker.endArchiving();

            JSONObject results = new JSONObject();
            JSONArray seedsArray = new JSONArray();
            seedsArray.addAll(worker.getFoundUnarchivedURLS());

            JSONArray completed = new JSONArray();
            completed.addAll(worker.getFoundUnarchivedURLS());

            JSONArray failedArray = new JSONArray();
            failedArray.addAll(worker.getFailedURLS());

            JSONArray excludedArray = new JSONArray();
            excludedArray.addAll(worker.getExcludedURLS());

            results.put("seeds", seedsArray);
            results.put("completed", completed);
            results.put("exclude", excludedArray);
            results.put("failed", failedArray);

            FileWriter file = null;
            try {
                file = new FileWriter( System.getProperty("user.dir") + File.separatorChar + "archives"
                        + File.separatorChar + archiveName + File.separatorChar + "output_worker_"+ worker.getID() + ".json");
                file.write(results.toJSONString());
                file.close();
            } catch (IOException e) {
                LogUtility.logError("Error writing to file: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
