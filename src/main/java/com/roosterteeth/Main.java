package com.roosterteeth;

import com.roosterteeth.hooks.OutputSafetyHook;
import com.roosterteeth.hooks.WorkersShutdownHook;
import com.roosterteeth.utility.LogUtility;
import com.roosterteeth.worker.ArchiveWorker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.InvalidArgumentException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {

        JSONObject urlsObject = null;

        int numWorkers = 1;
        String archiveName = "roosterteeth-site-archive";
        int depth = Integer.MAX_VALUE;
        String gridpath = null;
        //Process arguments
        if(Arrays.asList(args).contains("--h") || Arrays.asList(args).contains("--help")) {
            if(args.length > 1) {
                if(!args[0].equals("--h") && !args[0].equals("--help")){
                    throw new IllegalArgumentException("-h or --help is not first argument!");
                }
                //Argument after
                System.out.println("All info presented should match readme file.");
                switch (args[1]) {
                    case "urls":
                        System.out.println("Help: --urls");
                        System.out.print("The web archiver reads in what urls to run and exclude based up a json file in the same folder as the jar. Does not need to be named urls.json\n" +
                                "and name provided in the --urls argument.\n" +
                                "{\n" +
                                "\"seeds\": [\"https://roosterteeth.com/g/user/adam\"],\n" +
                                "\"exclude\": [\"https://roosterteeth.com/g/user/IowaHawkins\"],\n" +
                                "\"completed\": [\"https://roosterteeth.com/g/user/Gearskiller94\"],\n" +
                                "\"failed\": [\"https://roosterteeth.com/g/user/ClusterStorm97\"],\n" +
                                "}\n" +
                                "The seeds JSON array is the list of urls to start archiving with.\n" +
                                "\n" +
                                "The exclude JSON array is the list of urls to not archive, if encountered.\n" +
                                "\n" +
                                "The completed and failed JSON arrays only show on tool generated output JSONs that can be reused.\n" +
                                "\n" +
                                "The completed array is a list of urls that were successfully archived. The URLs in the array will not be archived.\n" +
                                "\n" +
                                "The failed array is a list of urls that failed to archive (encountered an error or were archiving on shutdown). The URLS\n" +
                                "in the array will be archived.");
                        break;
                    case "workers":
                        System.out.println("Help: --workers");
                        System.out.print("This tool supports multi-threading. With the workers argument you can specify how many instances of webdriver should be archiving.\n" +
                                "\n" +
                                "Each instance of webdriver will get an even split of the urls to be archived. To ensure that this remains balanced and\n" +
                                "prevent double work, after each depth. The next set of urls to be archived are combined and rebalanced across new workers.\n" +
                                "\n" +
                                "Each worker for each pass will create an archive.");
                        break;
                    case "name":
                        System.out.println("Help: --name");
                        System.out.print("If no name is provided by default the archives created will be called roosterteeth-site-archive_pass_(passnumber)_worker(workernumber)\n" +
                                "\n" +
                                "Using the --name argument you can specify what name instead of roosterteeth-site-archive. _pass_(passnumber)_worker(workernumber) will remain.");
                        break;
                    case "depth":
                        System.out.println("Help: --depth");
                        System.out.println("How deep should the archiver go. Pages on the RT site contain links to other relevant pages. Community users follow and\n" +
                                "are followed by other users we want to archive. The depth argument defines how many layers away from our seeds we should go.\n" +
                                "\n" +
                                "By default, the depth is the integer maximum (a very large number), a depth of 0 or 1 is just the seed urls. With depth 2\n" +
                                "all linked pages will be archived, but not the pages those pages link to. Use --depth in the command line.\n" +
                                "\n" +
                                "An archive will be created for each depth.");
                        break;
                    case "help":
                        System.out.println("Help: --h or --help");
                        System.out.println("Displays quick summary of all available arguments if not provided an argument." +
                                " If provided argument gives more detailed description of the argument.");
                    default:
                        throw new IllegalArgumentException("Unrecognized argument to get help info for: " + args[1]);
                }
            } else {
                System.out.println("Please read the readme file provided for further info.");
                System.out.println("--urls: The relative path (path from directory jar file is in) to the json defining urls to archive");
                System.out.println("--workers: The number of workers (threads) to use archiving");
                System.out.println("--name: The name of the folder the created archives will be under in the archives folder. If name is already used a number will be added at end");
                System.out.println("--depth: How many passes of archiving found urls. Value of 1 means just archive urls found in specified json.");
                System.out.println("--help or --h \"argument\". Get more detailed message on argument.");
            }

        }
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
                case "--help":
                case "--h":

                    return;
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
            archiveDirectory.mkdirs();
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
        final JSONArray originalExcluded = (JSONArray) urlsObject.get("exclude");
        JSONArray previouslyCompleted = (JSONArray) urlsObject.get("completed");
        JSONArray previouslyFailed = (JSONArray) urlsObject.get("failed");

        if(excluded == null){
            excluded = new JSONArray();
        }
        HashSet<String> uniqueSeed = new HashSet<>();
        if(seeds != null){
            uniqueSeed.addAll(seeds);
        }

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

        ExecutorService pool = Executors.newFixedThreadPool(numWorkers);

        do{
            LogUtility.logInfo(String.format("----------------Pass %d: num urls %d----------------", pass,uniqueSeed.size()));
            List<HashSet<String>> sets = partitionSet(uniqueSeed,numWorkers);

            List<ArchiveWorker> workers = new ArrayList<>();
            List<Thread> threads = new ArrayList<>();

            WorkersShutdownHook shutdownHook = new WorkersShutdownHook(numWorkers,archiveName,completed,originalExcluded);
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            List<Future> futures = new ArrayList<>();
            for(int i =0;i<numWorkers && i<sets.size();i++){
                //TODO remove pass naming scheme if importing archives is possible.
                workers.add(new ArchiveWorker(sets.get(i),excludedUrls,i,archiveName,pass,gridpath));
                Thread workerThread = new Thread(workers.getLast());
                futures.add(pool.submit(workerThread));
            }

            //Wait for all workers to end
            for(Future f : futures){
                try {
                    f.get();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            LogUtility.logInfo("Workers have finished!");
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
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }while (!uniqueSeed.isEmpty() && pass<=depth);
        pool.shutdown();
        LogUtility.logInfo("Finished archiving all URLS. Do not terminate program!");
        OutputSafetyHook outputSafetyHook = new OutputSafetyHook(uniqueSeed,completed,failed,excludedUrls,archiveName);
        Runtime.getRuntime().addShutdownHook(outputSafetyHook);

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

        final String fileName = System.getProperty("user.dir") + File.separatorChar + "archives" + File.separatorChar + archiveName + File.separatorChar + "output.json";

        FileWriter file = new FileWriter(fileName);
        file.write(results.toJSONString());
        file.close();
        Runtime.getRuntime().removeShutdownHook(outputSafetyHook);
        LogUtility.logInfo("Created output.json at " + fileName);
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
}