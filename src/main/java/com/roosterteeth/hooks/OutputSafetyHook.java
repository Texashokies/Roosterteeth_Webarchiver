package com.roosterteeth.hooks;

import com.roosterteeth.utility.LogUtility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

/**
 * A shutdown hook for when main is creating the output.json in case tool is shutdown while this is happening.
 */
public class OutputSafetyHook extends Thread{


    private HashSet<String> uniqueSeed;
    private HashSet<String> completed;
    private HashSet<String> failed;
    private HashSet<String> excludedUrls;
    private String archiveName;

    public OutputSafetyHook(HashSet<String> uniqueSeed, HashSet<String> completed, HashSet<String> failed, HashSet<String> excludedUrls,String archiveName){
        this.uniqueSeed = uniqueSeed;
        this.completed = completed;
        this.failed = failed;
        this.excludedUrls = excludedUrls;
        this.archiveName = archiveName;
    }

    /**
     * Creates the output.json based on sets from main.
     */
    @Override
    public void run(){
        LogUtility.logInfo("Tools closed before output json written. Writing output.json");
        JSONObject results = new JSONObject();
        JSONArray seedsArray = new JSONArray();
        seedsArray.addAll(uniqueSeed);
        results.put("seeds", seedsArray);

        JSONArray completedArray = new JSONArray();
        completedArray.addAll(completed);
        results.put("completed",completedArray);

        JSONArray excludedArray = new JSONArray();
        excludedArray.addAll(excludedUrls);
        results.put("exclude",excludedArray);

        JSONArray failedArray = new JSONArray();
        failedArray.addAll(failed);
        results.put("failed",failedArray);

        try{
            FileWriter file = new FileWriter( System.getProperty("user.dir") + File.separatorChar + "archives" + File.separatorChar + archiveName + File.separatorChar + "output.json");
            file.write(results.toJSONString());
            file.close();
        }catch(IOException e){

        }

    }
}
