package com.roosterteeth.hooks;

import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.worker.ArchiveWorker;

import java.util.HashSet;
import java.util.List;

public class WorkersShutdownHook extends Thread {


    private final List<ArchiveWorker> workers;
    private final List<Thread> threads;
    private final HashSet<String> completed;
    private final HashSet<String> excludedUrls;

    public WorkersShutdownHook(List<ArchiveWorker> workers, List<Thread> threads, HashSet<String> completed, HashSet<String> excludedUrls) {
        this.workers = workers;
        this.threads = threads;
        this.completed = completed;
        this.excludedUrls = excludedUrls;
    }

    public void run() {
        LogUtility.logInfo("Premature shutdown! Attempting to shutdown workers...");
        for(Thread thread : threads){
            thread.interrupt();
        }
        //Get worker results
        HashSet<String> unarchivedFoundPages = new HashSet<>();
        for(ArchiveWorker worker: workers){
            unarchivedFoundPages.addAll(worker.getFoundUnarchivedURLS());
        }

        for(ArchiveWorker worker: workers){
            completed.retainAll(worker.getArchivedURLS());
        }

        HashSet uniqueSeed = new HashSet<>();
        for(String url : unarchivedFoundPages){
            if(!excludedUrls.contains(url)){
                uniqueSeed.add(url);
            }
        }
    }
}
