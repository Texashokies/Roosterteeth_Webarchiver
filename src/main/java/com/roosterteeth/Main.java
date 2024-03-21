package com.roosterteeth;

import com.roosterteeth.worker.ArchiveWorker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {

        JSONObject urlsObject = null;

        int numWorkers = 1;
        String archiveName = "roosterteeth-site-archive";

        //Process arguments
        for(int i = 0;i< args.length;i+=2){
            String argument = args[i];
            switch (argument){
                case "--urls":
                    String urlsJsonRelativePath = args[i+1];
                    JSONParser parser = new JSONParser();
                    if(urlsJsonRelativePath.charAt(0) != File.separatorChar){
                        urlsJsonRelativePath = File.separatorChar + urlsJsonRelativePath;
                    }
                    urlsObject = (JSONObject) parser.parse(new FileReader(new File("").getAbsolutePath() + urlsJsonRelativePath));
                    break;
                case "--workers":
                    numWorkers = Integer.parseInt(args[i+1]);
                    if(numWorkers <= 0){
                        throw new IllegalArgumentException("Provided number of workers is 0 or negative!");
                    }
                    break;
                case "--name":
                    archiveName = args[i+1];
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument provided: " + argument);
            }
        }

        if(urlsObject == null){
            throw new IllegalArgumentException("No urls json provided!");
        }

        //Split up urls by number of workers
        JSONArray seeds = (JSONArray) urlsObject.get("seeds");
        JSONArray excluded = (JSONArray) urlsObject.get("exclude");

        if(excluded == null){
            excluded = new JSONArray();
        }

        HashSet<String> uniqueSeed = new HashSet<>(seeds);
        HashSet<String> excludedUrls = new HashSet<>(excluded);
        List<HashSet<String>> sets = partitionSet(uniqueSeed,numWorkers);

        List<ArchiveWorker> workers = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for(int i =0;i<numWorkers;i++){
            workers.add(new ArchiveWorker(sets.get(i),excludedUrls,i,archiveName));
           Thread workerThread = new Thread(workers.getLast());
           threads.add(workerThread);
           workerThread.start();
        }

        //Wait for all workers to end
        for(Thread thread:threads){
            thread.join();
        }
    }

    /**
     * Partitions the given hash set of strings into sets based on number of partitions
     * @param originalSet The original set to be partitioned
     * @param numPartitions The number of sets to partition the original set into
     * @return List of size numPartitions of hash sets.
     */
    private static List<HashSet<String>> partitionSet(HashSet<String> originalSet,double numPartitions){
        final double partitionSize = Math.ceil(originalSet.size() / numPartitions);

        List<HashSet<String>> sets = new ArrayList<>();

        Iterator<String> setIterator = originalSet.iterator();
        while(setIterator.hasNext()){
            HashSet<String> newSet = new HashSet<>();
            for(int i =0;i<partitionSize && setIterator.hasNext();i++){
                newSet.add(setIterator.next());
            }
            sets.add(newSet);
        }
        return sets;
    }

    //TODO add shutdown hook that will save progress of workers.
}