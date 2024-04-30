# Roosterteeth Web Archiver

This program is designed to use the Webrecorder Archive.Page Chrome extension to archive the Roosterteeth sites before they are taken down.

The program uses selenium to automate interacting with the webpages to create a good recording.


# Set up

Make sure JDK 21 is installed

Make sure Maven is upto date

Install the crx file of the Webrecorder ArchiveWebPage extension and place it in extensions folder same level as jar
https://chromewebstore.google.com/detail/webrecorder-archivewebpag/fpeoodllldobpkbkabpblcfaogecpndd

# Running
Based on command line arguments provided when running the jar the tool will open webdriver instances and archive the pages outline in your json file.
The archive collections will then be downloaded to archives/archivename. If you use the same name another folder will be created.

# Shutting Down
The tool will attempt to shut down gracefully if terminated while running. All running workers will save their archives
and output a json following the same urls.json format to allow for starting runs back up again. There will also be an output.json
created that should have the combined values of each individual workers output.json.

## urls.json
The web archiver reads in what urls to run and exclude based up a json file in the same folder as the jar
and name provided in the --urls argument.

`
{
"seeds": ["https://roosterteeth.com/g/user/adam"],
"exclude": ["https://roosterteeth.com/g/user/IowaHawkins"],
"completed": ["https://roosterteeth.com/g/user/Gearskiller94"],
"failed": ["https://roosterteeth.com/g/user/ClusterStorm97"],
}
`

The seeds JSON array is the list of urls to start archiving with.

The exclude JSON array is the list of urls to not archive, if encountered.

The completed and failed JSON arrays only show on tool generated output JSONs that can be reused.

The completed array is a list of urls that were successfully archived. The URLs in the array will not be archived.

The failed array is a list of urls that failed to archive (encountered an error or were archiving on shutdown). The URLS
in the array will be archived.


## depth
How deep should the archiver go. Pages on the RT site contain links to other relevant pages. Community users follow and
are followed by other users we want to archive. The depth argument defines how many layers away from our seeds we should go.

By default, the depth is the integer maximum (a very large number), a depth of 0 or 1 is just the seed urls. With depth 2
all linked pages will be archived, but not the pages those pages link to. Use --depth in the command line.

An archive will be created for each depth.

## Workers 
This tool supports multi-threading. With the workers argument you can specify how many instances of webdriver should be archiving.

Each instance of webdriver will get an even split of the urls to be archived. To ensure that this remains balanced and
prevent double work, after each depth. The next set of urls to be archived are combined and rebalanced across new workers.

Each worker for each pass will create an archive.

## Archive name
If no name is provided by default the archives created will be called roosterteeth-site-archive_pass_(passnumber)_worker(workernumber)

Using the --name argument you can specify what name instead of roosterteeth-site-archive. _pass_(passnumber)_worker(workernumber) will remain.

## Running on a selenium grid
The argument --grid exists but should not be used until downloading archives support is added.
