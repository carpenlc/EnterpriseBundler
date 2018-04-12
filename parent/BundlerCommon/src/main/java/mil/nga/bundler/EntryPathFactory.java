package mil.nga.bundler;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.PropertyLoader;
import mil.nga.bundler.exceptions.PropertiesNotLoadedException;
import mil.nga.bundler.interfaces.BundlerConstantsI;

/**
 * The "entry path" is the path (or location) within an output archive where 
 * a target file is stored.  The basic algorithm implemented executes the 
 * following steps:
 * 
 * 1.  We strip off everything but the absolute file path.
 * 2.  Once we have the absolute path to an output file, we strip off any pre-
 *     defined path prefixes that were included in the properties file.
 * 3.  Next, we ensure that the output file entry is not over 100 characters.
 *     If it's longer than 100 characters, we start eliminating path elements 
 *     from the front.  If we eliminate all the path entries and it's still
 *     longer than 100 characters, we truncate the filename keeping the 
 *     extension (if it contains an extension).
 * 
 * @author L. Craig Carpenter
 */
public class EntryPathFactory 
        extends PropertyLoader 
        implements BundlerConstantsI {

    /**
     * An output entry path cannot be longer than 100 characters.
     */
    public static final int ENTRY_PATH_LENGTH_LIMIT = 100;
    
    /**
     * Set up the Log4j system for use throughout the class
     */        
    static final Logger LOGGER = LoggerFactory.getLogger(
            EntryPathFactory.class);
    
    /**
     * List of path prefixes to exclude
     */
    private List<String> prefixExclusions = null;
    
    /**
     * Private constructor that forces the singleton design pattern and 
     * loads any relevant properties from an external file.
     */
    private EntryPathFactory() {
        super(PROPERTY_FILE_NAME);
        try {
            loadPrefixMap(getProperties());
        }
        catch (PropertiesNotLoadedException pnle) {
            LOGGER.warn("An unexpected PropertiesNotLoadedException " 
                    + "was encountered.  Please ensure the application "
                    + "is properly configured.  Exception message [ "
                    + pnle.getMessage()
                    + " ].");
        }
    }
    
    /**
     * Alternate private constructor added to support jUnit testing.  This 
     * constructor allows clients to supply a Properties object as opposed 
     * to loading an external properties file.
     * 
     * @param props Populated properties file.
     */
    private EntryPathFactory(Properties props) {
        if (props != null) {
            loadPrefixMap(props);
        }
        else {
            LOGGER.warn("Input Properties object is null.");
        }
    }
    
    /**
     * Method used to load the List of path prefixes that are to be excluded
     * from the entry path that will exist in the output archive file.
     * 
     * @param props Populated properties file. 
     */
    private void loadPrefixMap(Properties props) {
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
                        LOGGER.debug("Found prefix exclusion [ "
                                + exclusion 
                                + " ] in property [ "
                                + PARTIAL_PROP_NAME 
                                + Integer.toString(i).trim()
                                + " ].");
                    }
                }
            }
        }
        else {
            LOGGER.error("Input Properties object is null.  No prefix "
                    + "exclusions loaded.");
        }
    }
    
    /**
     * This method enforces the 100-character limit for the entry path.
     * 
     * @param path The input candidate entry path.
     * @return An entry path conforming to the length limitation.
     */
    public String enforceLengthLimit(String path) {
        
        String limitedPath = path;
        
        if ((limitedPath != null) && (!limitedPath.isEmpty())) {
            if (limitedPath.length() > ENTRY_PATH_LENGTH_LIMIT) {
                do {
                    if (limitedPath.contains("/")) {
                        limitedPath = limitedPath.substring(limitedPath.indexOf("/")+1);
                    }
                    else {
                        limitedPath = truncateFilename(limitedPath);
                    }
                }
                while (limitedPath.length() > ENTRY_PATH_LENGTH_LIMIT);
            }
        }
        
        return limitedPath;
    }
    
    /**
     * Extract and return the extension from the input file path.
     * @param path The input file path.
     * @return The extension (with the "." separator)
     */
    public String getExtension(String path) {
        String extension = "";
        if ((path == null) || (path.isEmpty())) {
            return extension;
        }
        int dotPos = path.lastIndexOf(".");
        if ( dotPos < 0 )
            return extension;
        int dirPos = path.lastIndexOf( File.separator );
        if ( dirPos < 0 && dotPos == 0 )
            return extension;
        if ( dirPos >= 0 && dirPos > dotPos )
            return extension;
        extension = path.substring(dotPos);
        return extension;
    }
    
    /**
     * Check to see if the input file path contains a file extension.
     * 
     * @param path A full file path.
     * @return True if the file contains an extension, false otherwise.
     */
    public static boolean hasExtension(String path) {
        if ((path == null) || (path.isEmpty())) {
            return false;
        }
        int dotPos = path.lastIndexOf(".");
        if ( dotPos < 0 )
            return false;
        int dirPos = path.lastIndexOf( File.separator );
        if ( dirPos < 0 && dotPos == 0 )
            return false;
        if ( dirPos >= 0 && dirPos > dotPos )
            return false;
        return true;
    }

    /**
     * String manipulation function to remove any extensions from the input
     * archive file designator.  The archiver classes will add an extension 
     * based on the type of archive that was requested.
     * 
     * @param path The full path to the output archive file.
     * @return The path sans extensions.
     */
    private String removeExtension(String path) {
        int dotPos = path.lastIndexOf(".");
        if (dotPos < 0) {
            return path;
        }
        int dirPos = path.lastIndexOf(File.separator);
        if ((dirPos < 0) && (dotPos == 0)) {
            return path;
        }
        if ((dirPos >= 0) && (dirPos > dotPos)) {
            return path;
        }
        return path.substring(0, dotPos);
    }
    
    /**
     * This method does the heavy lifting associated with stripping off any 
     * configured prefixes and ensuring the output entry path does not start
     * with a file separator character.
     * 
     * @param path The actual file path.
     * @return The calculated entry path.
     */
    private String stripPredefinedExclusions(String path) {
        
        String entryPath = path;
        
        if ((prefixExclusions != null) && (prefixExclusions.size() > 0)) {
            for (String exclusion : prefixExclusions) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Testing for exclusion [ "
                            + exclusion
                            + " ].");
                }
                if (entryPath.startsWith(exclusion)) {
                    entryPath = entryPath.replaceFirst(Pattern.quote(exclusion), "");
                }
            }
        } 
        else {
            LOGGER.warn("There are no prefix exclusions available to apply "
                    + "to the input File path.");
        }
        // Ensure the path does not start with a path separator character.
        if (entryPath.startsWith(System.getProperty("file.separator"))) {
            entryPath = entryPath.replaceFirst(Pattern.quote(
                    System.getProperty("file.separator")), "");
        }
        return entryPath;
    }
    
    /**
     * Method used to truncate an input filename down to the required 100
     * character limit.  
     * 
     * @param path The candidate path.
     * @return The truncated filename.
     */
    public String truncateFilename(String path) {
        
        String truncated = removeExtension(path);
        String extension = getExtension(path);
        truncated = truncated.substring(
                0, 
                ENTRY_PATH_LENGTH_LIMIT - extension.length());
        
        return truncated + extension;
    }
    
    /**
     * Calculate the entry path associated with the input String-based 
     * absolute path to a target file.
     * 
     * @param path The String based absolute path.
     * @return The String-based entry path.
     */
    public String getEntryPath(String path) {
        String entryPath = null;
        if ((path != null) && (!path.isEmpty())) {
            entryPath = stripPredefinedExclusions(path);
            entryPath = enforceLengthLimit(entryPath);
        }
        return entryPath;
    }
    
    /**
     * Calculate the entry path associated with the input URI.  Only the 
     * absolute path (not the scheme, host, etc.) is included in the 
     * calculation of the entry path.
     * 
     * @param uri URI of the target file.
     * @return The String-based entry path.
     */
    public String getEntryPath(URI uri) {
        String entryPath = null;
        if ((uri != null) && 
                (uri.getPath() != null) && 
                (!uri.getPath().isEmpty())) {
            entryPath = getEntryPath(uri.getPath());
        }
        else {
            LOGGER.warn("Invalid URI. Unable to retrieve the absolute path "
                    + "from the target URI.");
        }
        return entryPath;
    }
    
    /**
     * Calculate the entry path associated with the input URI.  Only the 
     * absolute path (not the scheme, host, etc.) is included in the 
     * calculation of the entry path.
     * 
     * @param uri URI of the target file.
     * @param replacementPath The string to use to replace the full path.
     * @return The String-based entry path.
     */
    public String getEntryPath(URI uri, String replacementPath) {
        StringBuilder entryPath = new StringBuilder();
        if ((uri != null) && 
                (uri.getPath() != null) && 
                (!uri.getPath().isEmpty())) {
            Path p = Paths.get(uri);
            if ((replacementPath == null) || (replacementPath.isEmpty())) {
                entryPath.append(p.getFileName());
            }
            else {
                entryPath.append(replacementPath);
                if (!entryPath.toString().endsWith(File.separator)) {
                    entryPath.append(File.separator);
                }
                entryPath.append(p.getFileName());
            }
        }
        else {
            LOGGER.warn("Invalid URI. Unable to retrieve the absolute path "
                    + "from the target URI.");
        }
        return entryPath.toString();
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
            URI uri, 
            String baseDir, 
            String archivePath) {
        
        String entryPath = null;
        
        if ((uri != null) && 
                (uri.getPath() != null) && 
                (!uri.getPath().isEmpty())) {
            
            entryPath = uri.getPath();
            if ((entryPath != null) && (!entryPath.isEmpty())) {
                
                // treat the baseDir as an exclusion from the absolute path.
                if ((baseDir != null) && (!baseDir.isEmpty())) {
                    
                    // Treat the baseDir as an exclusion
                    if (entryPath.startsWith(baseDir)) {
                        entryPath = entryPath.replaceFirst(
                                Pattern.quote(baseDir), 
                                "");
                    }
                }
                
                // If the archive path is supplied, append it to whatever is 
                // left over.
                if ((archivePath != null) && (!archivePath.isEmpty())) {
                    
                    // Make sure the current entryPath doesn't start with a 
                    // file separator char.  This ensures there are not 
                    // duplicate file separator characters.
                    if (entryPath.startsWith("/")) { 
                        entryPath = entryPath.replaceFirst(
                                Pattern.quote("/"), 
                                "");
                    }
                    
                    // Make sure the archivePath doesn't end with a file 
                    // separator char this ensures there are not duplicates.
                    if (archivePath.endsWith("/")) {
                        archivePath = archivePath.substring(
                                0, 
                                archivePath.length()-1);
                    }
                    entryPath = archivePath+"/"+entryPath;
                }
            }
            else {
                LOGGER.warn("Unable to extract file path from URI.  URI "
                        + "provided [ "
                        + uri.toString()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("Invalid URI. Unable to retrieve the absolute path "
                    + "from the target URI.");
        }
        return getEntryPath(entryPath);
    }
    
    /**
     * Getter method for the prefix exclusions that were read from the input
     * Properties object.
     * @return A list of Strings to exclude from the calculated entry paths.
     */
    public List<String> getPrefixExclusions() {
        return prefixExclusions;
    }
    
    /**
     * Getter method for the singleton instance of the EntryPathFactory.
     * @return Handle to the singleton instance of the EntryPathFactory.
     */
    public static EntryPathFactory getInstance() {
        return EntryPathFactoryHolder.getFactorySingleton();
    }
    
    /**
     * Getter method for the singleton instance of the EntryPathFactory.
     * @return Handle to the singleton instance of the EntryPathFactory.
     */
    public static EntryPathFactory getInstance(Properties props) {
        return new EntryPathFactory(props);
    }
    
    /** 
     * Static inner class used to construct the factory singleton.  This
     * class exploits that fact that inner classes are not loaded until they 
     * referenced therefore enforcing thread safety without the performance 
     * hit imposed by the use of the "synchronized" keyword.
     * 
     * @author L. Craig Carpenter
     */
    public static class EntryPathFactoryHolder {
        
        /**
         * Reference to the Singleton instance of the factory
         */
        private static EntryPathFactory factory = null;
        
        /**
         * Accessor method for the singleton instance of the factory object.
         * @return The singleton instance of the factory.
         */
        public static EntryPathFactory getFactorySingleton() {
            if (factory == null) {
                factory = new EntryPathFactory();
            }
            return factory;
        }
        
        /**
         * Accessor method for the singleton instance of the factory object.
         * @param props Properties object.
         * @return The singleton instance of the factory.
         */
        public static EntryPathFactory getFactorySingleton(Properties props) {
            if (factory == null) {
                factory = new EntryPathFactory(props);
            }
            return factory;
        }
    }
}
