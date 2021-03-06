package mil.nga.bundler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import mil.nga.PropertyLoader;
import mil.nga.bundler.exceptions.PropertiesNotLoadedException;
import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.model.ArchiveJob;
import mil.nga.bundler.model.FileEntry;
import mil.nga.util.URIUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for configuring the path inside of an output 
 * archive in which a given file will be inserted.  It's main use is to 
 * exclude certain directory prefixes that are configurable in an external 
 * properties file.  
 * 
 * @author L. Craig Carpenter
 *
 */
public class PathGenerator 
        extends PropertyLoader 
        implements BundlerConstantsI {
    
    /**
     * Set up the Log4j system for use throughout the class
     */        
    Logger LOGGER = LoggerFactory.getLogger(PathGenerator.class);
    
    /**
     * List of path prefixes to exclude
     */
    private List<String> prefixExclusions = null;
    
    /**
     * Private constuctor enforcing the singleton design pattern.
     */
    private PathGenerator() {
        super(PROPERTY_FILE_NAME);
        try {
            loadPrefixMap(getProperties());
        }
        catch (PropertiesNotLoadedException pnle) {
            LOGGER.warn("An unexpected PropertiesNotLoadedException " 
                    + "was encountered.  Please ensure the application "
                    + "is properly configured.  Exception message [ "
                    + pnle.getMessage()
                    + " ].  Paths will not be molested.");
        }
    }
    
    /**
     * Method used to load the List of path prefixes that are to be excluded
     * from the entry path that will exist in the output archive file.
     * 
     * @param props Populated properties file. 
     */
    private void loadPrefixMap(Properties props) {
        
        String method = "loadPrefixMap() - ";
        
        if (props != null) {
            if (prefixExclusions == null) {
                prefixExclusions = new ArrayList<String>();
            }
            for (int i=0; i<MAX_NUM_EXCLUSIONS; i++) {
                String exclusion = props.getProperty(
                        PARTIAL_PROP_NAME + Integer.toString(i).trim());
                if ((exclusion != null) && (!exclusion.isEmpty())) {
                    prefixExclusions.add(exclusion);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(method
                                + "Found prefix exclusion [ "
                                + exclusion 
                                + " ] in property [ "
                                + PARTIAL_PROP_NAME + Integer.toString(i).trim()
                                + " ].");
                    }
                }
            }
        }
        else {
            LOGGER.error(method 
                    + "Input Properties object is null.  No prefix exclusions "
                    + "loaded.");
        }
    }
    
    /**
     * This method does the heavy lifting associated with stripping off any 
     * configured prefixes and ensuring the output entry path does not start
     * with a file separator character.
     * 
     * @param path The actual file path.
     * @return The calculated entry path.
     */
    private String getEntryPath(String path) {
        
        String method = "getEntryPath() - ";
        String entryPath = path;
        
        if ((prefixExclusions != null) && (prefixExclusions.size() > 0)) {
            for (String exclusion : prefixExclusions) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(method 
                            + "Testing for exclusion [ "
                            + exclusion
                            + " ].");
                }
                if (entryPath.startsWith(exclusion)) {
                    entryPath = entryPath.replaceFirst(Pattern.quote(exclusion), "");
                }
            }
            
            // Ensure the path does not start with a path separator character.
            if (entryPath.startsWith(System.getProperty("file.separator"))) {
                entryPath = entryPath.replaceFirst(Pattern.quote(
                        System.getProperty("file.separator")), "");
            }
        } 
        else {
            LOGGER.warn(method 
                    + "There are no prefix exclusions available to apply to "
                    + "the input File path.");
        }
        
        return entryPath;
    }
    
    /**
     * Private method that takes one FileEntry object, reads the identified 
     * full path, and then strips off any path prefixes that were identified 
     * as needing stripped by the application properties file.
     * 
     * @param entry FileEntry object associated with one file to be archived.
     */
    public void setOneEntry(FileEntry entry) {

        String path;
        
        // Use the URI class to strip off the scheme/authority sections.
        try {
            URI uri = new URI(entry.getFilePath());
            path = uri.getPath();
        }
        catch (URISyntaxException use) {
            LOGGER.warn("Unable to convert the absolute file path [ " 
                    + entry.getFilePath() 
                    + " ] to a URI.  Using the absolute file path as-is.  "
                    + "Exception message => [ "
                    + use.getMessage()
                    + " ].");
            path   = entry.getFilePath();
        }
        
        if ((entry.getEntryPath() == null) || (entry.getEntryPath().isEmpty())) {
            // If the entry path wasn't supplied, calculate it.
            entry.setEntryPath(getEntryPath(path.trim()));
        }
        else {
            // If the entry path was supplied by the client, make sure it 
            // doesn't start with a file separator character.
            if (entry.getEntryPath().startsWith("/")) {
                entry.setEntryPath(entry.getEntryPath().substring(1));
            }
        }
    }
    
    /**
     * Public entry point.  The client supplies a single Archive.  This 
     * method loops through all of the files identified for archive and sets 
     * their entry path in the output archive.
     *  
     * @param archive A single Archive to be sent to the bundler process.
     */
    public void setPaths(ArchiveJob archive) {
        if ((archive.getFiles() != null) && 
                (archive.getFiles().size() > 0)) {
            for (FileEntry entry : archive.getFiles()) {
                setOneEntry(entry);
            }
        }
        else {
            LOGGER.warn("The current Archive objects does not contain "
                    + "a list of files to compress.");
        }
    }
    
    /**
     * This ugly method is used to calculate the entry path within the output
     * archive for files that were identified by searching through nested 
     * directories.  The basic algorithm is that the base directory is 
     * excluded (i.e. eliminated) from the absolute path.  The archivePath 
     * (if supplied) is then prepended to what is left of the absolute path.
     * 
     * @param baseDir The base directory which was the starting point for 
     * the file search that resulted in the absolutePath.
     * @param archivePath The user-supplied archivePath.
     * @param absolutePath The absolute path to a single file.
     * @return The entry path for a single file.
     */
    public String getEntryPath(
            String baseDir, 
            String archivePath, 
            String absolutePath) {
        
        // Use the URI class to strip off the scheme/authority sections.
        try {
            URI uri = new URI(absolutePath);
            absolutePath = uri.getPath();
        }
        catch (URISyntaxException use) {
            LOGGER.warn("Unable to convert the absolute file path [ " 
                    + absolutePath 
                    + " ] to a URI.  Using the absolute file path as-is.  "
                    + "Exception message => [ "
                    + use.getMessage()
                    + " ].");
        }

        String entryPath = absolutePath;
        
        // TODO: Test code.  Remove.
        LOGGER.info("getEntryPath() called with baseDir => [ "
                + baseDir
                + " ], archivePath => [ "
                + archivePath
                + " ], and absolutePath => [ "
                + absolutePath 
                + " ].");
        
        // if the archivePath isn't supplied, do nothing.
        // If the archive path is supplied, append it to whatever is left over.
        if ((archivePath != null) && (!archivePath.isEmpty())) {
            
            // treat the baseDir as an exclusion from the absolute path.
            if ((baseDir != null) && (!baseDir.isEmpty())) {
                // Treat the baseDir as an exclusion
                if (absolutePath.startsWith(baseDir)) {
                    entryPath = absolutePath.replaceFirst(Pattern.quote(baseDir), "");
                }
            }    
        
            // Make sure the archivePath doesn't end with a file separator char 
            // this ensures there are not duplicates.
            if (archivePath.endsWith("/")) {
                archivePath = archivePath.substring(0,archivePath.length()-1);
            }
            
            // Make sure whatever is left of the absolutePath does not start 
            // with a file separator character.
            if (entryPath.startsWith("/")) {
                entryPath = entryPath.substring(1);
            }
            
            entryPath = archivePath+"/"+entryPath;
        
        }
        else {
            entryPath = getEntryPath(absolutePath);
        }
        
        // TODO: Test code. Remove.
        LOGGER.info("getEntryPath() returning [ "
                + entryPath
                + " ].");
        
        return entryPath;
    }
    
    /**
     * Public entry point.  The client supplies a List of Archives that will 
     * be sent to the bundler process.  This method loops through all of the 
     * files identified for archive and sets their entry path in the archive.
     *  
     * @param archives List of Archives to be sent to the bundler process.
     */
    public void setPaths(List<ArchiveJob> archives) {
        String method = "setPaths() - ";
        if ((archives != null) && (archives.size() > 0)) {
            for (ArchiveJob archive : archives) {
                if ((archive.getFiles() != null) && 
                        (archive.getFiles().size() > 0)) {
                    for (FileEntry entry : archive.getFiles()) {
                        setOneEntry(entry);
                    }
                }
                else {
                    LOGGER.warn(method 
                            + "The current Archive objects does not contain "
                            + "a list of files to compress.");
                }
            }
        }
        else {
            LOGGER.warn(method 
                    + "The input list of Archives is null or contains zero "
                    + "entries.");
        }
    }
    
    /**
     * Accessor method for the singleton instance of the AeroDataFactory.
     * @return Handle to the singleton instance of the AeroDataFactory.
     */
    public static PathGenerator getInstance() {
        return PathFactoryHolder.getFactorySingleton();
    }
    
    /** 
     * Static inner class used to construct the factory singleton.  This
     * class exploits that fact that inner classes are not loaded until they 
     * referenced therefore enforcing thread safety without the performance 
     * hit imposed by the use of the "synchronized" keyword.
     * 
     * @author L. Craig Carpenter
     */
    public static class PathFactoryHolder {
        
        /**
         * Reference to the Singleton instance of the factory
         */
        private static PathGenerator _factory = new PathGenerator();
        
        /**
         * Accessor method for the singleton instance of the factory object.
         * @return The singleton instance of the factory.
         */
        public static PathGenerator getFactorySingleton() {
            return _factory;
        }
    }
    
    
    public static void main(String[] args) {
        
        PathGenerator.getInstance();
        FileEntry entry = new FileEntry();
        entry.setFilePath("/mnt/raster/dir1/dir2/dir3");
        entry.setEntryPath(null);
        PathGenerator.getInstance().setOneEntry(entry);
        System.out.println(entry.toString());
        
        
        String test = PathGenerator.getInstance().getEntryPath("/1/2/3/4", "/7/8/", "/1/2/3/4/5/6/file.txt");
        System.out.println(test);
        test = PathGenerator.getInstance().getEntryPath("/1/2/3/4", "/7/8", "/1/2/3/4/5/6/file.txt");
        System.out.println(test);
        test = PathGenerator.getInstance().getEntryPath("/1/2/3/4", "", "/1/2/3/4/5/6/file.txt");
        System.out.println(test);
        test = PathGenerator.getInstance().getEntryPath("/1/2/3/4", "/7/8/4", "/1/2/3/4/5/6/file.txt");
        System.out.println(test);
        test = PathGenerator.getInstance().getEntryPath("/1/2/3/4", null, "/mnt/raster/1/2/3/4/5/6/file.txt");
        System.out.println(test);
    }
}
