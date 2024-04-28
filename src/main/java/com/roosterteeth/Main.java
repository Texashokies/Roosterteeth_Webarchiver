package com.roosterteeth;

import com.roosterteeth.hooks.WorkersShutdownHook;
import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.worker.ArchiveWorker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
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
        int depth = Integer.MAX_VALUE;
        String gridpath = null;
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
                case "--depth":
                    depth = Integer.parseInt(args[i+1]);
                    if(depth<=0){
                        throw new IllegalArgumentException("Provided depth is 0 or negative");
                    }
                    break;
                case "--grid":
                    throw  new IllegalArgumentException("Running on grid not currently supported. Check for newer release!");
                    //TODO uncomment out once support for downloading archives from grid is added.
//                    gridpath = args[i+1];
//                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument provided: " + argument);
            }
        }

        final String originalArchiveName = archiveName;
        //Check archive name isn't already used.
        File archiveDirectory = new File(System.getProperty("user.dir") + File.separatorChar + "archives" + File.separatorChar + archiveName);
        Path archivePath = archiveDirectory.toPath();

        int fileNameIndex = 1;
        try{
            while(Files.list(archivePath).count() > 0){
                archiveName = originalArchiveName +"_"+ fileNameIndex;
                archiveDirectory =  new File(System.getProperty("user.dir") + File.separatorChar + "archives" + File.separatorChar + archiveName);
                archivePath = archiveDirectory.toPath();
                fileNameIndex++;
            }
        }catch(NoSuchFileException e){
            //Directory does not exist.
            archiveDirectory.mkdir();
        }

        runWorkers(urlsObject,numWorkers,archiveName,depth,gridpath);
    }

    /**
     * Starts and runs the archive workers.
     * @param urlsObject The json containing list of urls to archive and exclude from archive
     * @param numWorkers The number of workers (threads) to use
     * @param archiveName The name of the archive
     * @param depth How deep should the archiving go
     * @throws InterruptedException Worker threads may be interrupted
     */
    private static void runWorkers(JSONObject urlsObject,int numWorkers,String archiveName,int depth,String gridpath) throws InterruptedException, IOException {
        if(urlsObject == null){
            throw new IllegalArgumentException("No urls json provided!");
        }

        //Split up urls by number of workers
        JSONArray seeds = (JSONArray) urlsObject.get("seeds");
        JSONArray excluded = (JSONArray) urlsObject.get("exclude");
        JSONArray previouslyCompleted = (JSONArray) urlsObject.get("completed");
        JSONArray previouslyFailed = (JSONArray) urlsObject.get("failed");

        if(excluded == null){
            excluded = new JSONArray();
        }

        HashSet<String> uniqueSeed = new HashSet<>(seeds);
        if(previouslyFailed != null){
            uniqueSeed.addAll(previouslyFailed);
        }
        HashSet<String> excludedUrls = new HashSet<>(excluded);
        if(previouslyCompleted != null){
            excludedUrls.addAll(previouslyCompleted);
        }

        HashSet<String> completed = new HashSet<>();
        HashSet<String> failed = new HashSet<>();

        int pass = 1;

        do{
            LogUtility.logInfo(String.format("----------------Pass %d: num urls %d----------------", pass,uniqueSeed.size()));
            List<HashSet<String>> sets = partitionSet(uniqueSeed,numWorkers);

            List<ArchiveWorker> workers = new ArrayList<>();
            List<Thread> threads = new ArrayList<>();

//            WorkersShutdownHook shutdownHook = new WorkersShutdownHook(workers,threads,completed,excludedUrls);
//            Runtime.getRuntime().addShutdownHook(shutdownHook);

            for(int i =0;i<numWorkers && i<sets.size();i++){
                //TODO remove pass naming scheme if importing archives is possible.
                workers.add(new ArchiveWorker(sets.get(i),excludedUrls,i,archiveName,pass,gridpath));
                Thread workerThread = new Thread(workers.getLast());
                threads.add(workerThread);
                workerThread.start();
            }

            //Wait for all workers to end
            for(Thread thread:threads){
                thread.join();
            }
            //get results from workers
            HashSet<String> unarchivedFoundPages = new HashSet<>();
            for(ArchiveWorker worker: workers){
                unarchivedFoundPages.addAll(worker.getFoundUnarchivedURLS());
                failed.addAll(worker.getFailedURLS());
                completed.addAll(worker.getArchivedURLS());
            }

            HashSet<String> alreadyArchivedAndExcluded = new HashSet<>();
            alreadyArchivedAndExcluded.addAll(excludedUrls);
            alreadyArchivedAndExcluded.addAll(uniqueSeed);

            excludedUrls = alreadyArchivedAndExcluded;

            uniqueSeed = new HashSet<>();
            for(String url : unarchivedFoundPages){
                if(!excludedUrls.contains(url)){
                    uniqueSeed.add(url);
                }
            }
            pass++;
            //Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }while (!uniqueSeed.isEmpty() && pass<=depth);

        //TODO need to do something about shutdown hooks so this is also safe if closed.
        JSONObject results = new JSONObject();
        JSONArray seedsArray = new JSONArray();
        seedsArray.addAll(uniqueSeed);
        results.put("seeds", seedsArray);
        JSONArray completedArray = new JSONArray();
        completedArray.addAll(completed);
        JSONArray failedArray = new JSONArray();
        failedArray.addAll(failed);
        results.put("completed",completedArray);
        results.put("exclude",excludedUrls);
        results.put("failed",failedArray);

        //Check if output.json exists
        FileWriter file = new FileWriter( System.getProperty("user.dir") + File.separatorChar + "archives" + File.separatorChar + archiveName + File.separatorChar + "output.json");
        file.write(results.toJSONString());
        file.close();
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