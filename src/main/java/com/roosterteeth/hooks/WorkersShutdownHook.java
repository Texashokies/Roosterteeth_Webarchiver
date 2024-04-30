package com.roosterteeth.hooks;

import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.utility.WaitHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A shutdown hook for main that will collect individual worker results.
 */
public class WorkersShutdownHook extends Thread {

    private final int numWorkers;
    private final String archiveName;
    private final HashSet<String> completed;
    private final JSONArray excluded;

    public WorkersShutdownHook(int numWorkers, String archiveName, HashSet<String> completed, JSONArray originalExcluded) {
        this.numWorkers = numWorkers;
        this.archiveName = archiveName;
        this.completed = completed;
        excluded = originalExcluded;
    }

    /**
     * Waits for worker json files to be created and combines them into output.json along with previous run completed urls.
     */
    public void run() {
        LogUtility.logInfo("Premature shutdown! Waiting for workers to shutdown.");

        List<File> outputFiles = new ArrayList<>();
        for(int i = 0; i < numWorkers; i++) {
            final String outputPath = System.getProperty("user.dir") + File.separatorChar + "archives"
                    + File.separatorChar + archiveName + File.separatorChar + "output_worker_"+ i + ".json";
            WaitHelper.waitForFileToDownload(outputPath, Duration.ofSeconds(10));
            outputFiles.add(new File(outputPath));
        }

        JSONParser parser = new JSONParser();

        HashSet<String> failed = new HashSet<>();
        HashSet<String> seeds = new HashSet<>();

        for(File outputFile : outputFiles) {
            try {
                JSONObject output = (JSONObject) parser.parse(new FileReader(outputFile));
                completed.addAll((JSONArray)output.get("completed"));
                failed.addAll((JSONArray)output.get("failed"));
                seeds.addAll((JSONArray)output.get("seeds"));
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }

        JSONObject combinedOutput = new JSONObject();
        JSONArray seedsArray = new JSONArray();
        seedsArray.addAll(seeds);
        combinedOutput.put("seeds", seedsArray);
        if(excluded != null) {
            JSONArray excludedArray = new JSONArray();
            excludedArray.addAll(excluded);
            combinedOutput.put("excluded", excludedArray);
        }
        if(completed != null) {
            JSONArray completedArray = new JSONArray();
            completedArray.addAll(completed);
            combinedOutput.put("completed", completedArray);
        }
        if(failed != null) {
            JSONArray failedArray = new JSONArray();
            failedArray.addAll(failed);
            combinedOutput.put("failed", failedArray);
        }

        FileWriter file;
        try {
            file = new FileWriter( System.getProperty("user.dir") + File.separatorChar + "archives"
                    + File.separatorChar + archiveName + File.separatorChar + "output.json");
            file.write(combinedOutput.toJSONString());
            file.close();
        } catch (IOException e) {
            LogUtility.logError("Error writing to file: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
