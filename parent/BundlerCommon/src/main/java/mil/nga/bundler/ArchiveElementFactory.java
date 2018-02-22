package mil.nga.bundler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.bundler.messages.FileRequest;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.util.FileFinder;
import mil.nga.util.URIUtils;

/**
 * The bundler interface (<code>BundlerI</code>) has a single method that 
 * accepts a list of <code>ArchiveElement</code> objects and an output 
 * file name.  This class was implemented to convert user-supplied lists 
 * of files to the require list of ArchiveElement objects.
 * 
 * @author L. Craig Carpenter
 */
public class ArchiveElementFactory {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    final static Logger LOGGER = LoggerFactory.getLogger(
            ArchiveElementFactory.class);
    
    /**
     * Constructor used to ensure the required file system drivers
     * are loaded.
     */
    public ArchiveElementFactory() {
        FileSystemFactory.getInstance().loadS3Filesystem();
        FileSystemFactory.getInstance().listFileSystemsAvailable();
    }
    
    /**
     * Alternate constructor added to support jUnit tests.
     * @param props The input Properties object should contain the
     * AWS configuration data.
     */
    public ArchiveElementFactory(Properties props) {
        FileSystemFactory.getInstance(props).loadS3Filesystem();
        FileSystemFactory.getInstance(props).listFileSystemsAvailable();
    }
    
    /**
     * Create a full URI based on an input String-based file path.
     * 
     * @param filePath Path to a target file.
     * @return Associated URI to the same target file.
     * @throws FileNotFoundException Thrown if the target file does not exist.
     * @throws FileSystemNotFoundException Thrown if the input URI resides on 
     * a file system that is not available.
     */
    public URI getURI(String filePath) 
            throws FileNotFoundException, FileSystemNotFoundException {
        URI uri = null;
        if ((filePath != null) && (!filePath.isEmpty())) {
            uri = URIUtils.getInstance().getURI(filePath);
            LOGGER.info("String: " + filePath);
            LOGGER.info("URI: " + uri.toString());
            if (!Files.exists(Paths.get(uri))) {
                throw new FileNotFoundException("Target file does not exist [ "
                        + uri.toString()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("Input filePath is null or not defined.  Returned "
                    + "URI will be null.");
        }
        return uri;
    }
    
    /**
     * Calculate the archive entry path for the given input String-based path.
     * It turns out that calculating the entry path is fairly complicated.  As 
     * such it has been off-loaded to a separate class.
     * 
     * @param filePath String-based path to the target file.
     * @return The entry path that will be used to archive the product.
     */
    private String getEntryPath(URI uri) {
        return EntryPathFactory.getInstance()
                .getEntryPath(uri);
    }
    
    /**
     * Calculate the archive entry path for the given input String-based path.
     * It turns out that calculating the entry path is fairly complicated.  As 
     * such it has been off-loaded to a separate class.  This method signature
     * replaces the entire file path with the input user-specified path.
     * 
     * @param filePath String-based path to the target file.
     * @return The entry path that will be used to archive the product.
     */
    private String getEntryPath(URI uri, String replacementPath) {
        return EntryPathFactory.getInstance()
                .getEntryPath(uri, replacementPath);
    }
    
    /**
     * Calculate the archive entry path for the given input String-based path.
     * It turns out that calculating the entry path is fairly complicated.  As 
     * such it has been off-loaded to a separate class.
     * 
     * @param uri The fill URI to the target file.
     * @param baseDir The base directory path.
     * @param archivePath User-specified string that will be pre-pended to the
     * entry path.
     * @return The entry path that will be used to archive the product.
     */
    private String getEntryPath(
            URI uri, 
            String baseDir, 
            String archivePath) {
        return EntryPathFactory
                .getInstance()
                .getEntryPath(uri, baseDir, archivePath);
    }
    
    /**
     * Generate a single ArchiveElement object from an input file that was 
     * derived from a directory traversal. 
     * 
     * @param file The target file for bundling.
     * @param baseDir The parent base-directory.
     * @param replacementPath Path that will be used to replace the target 
     * base directory.
     * @return An associated ArchiveElement object.
     */
    public ArchiveElement getArchiveElement(
            String file, 
            String baseDir, 
            String replacementPath) {
        
        ArchiveElement element = null;
        
        if ((file != null) && (!file.isEmpty())) {
            try {
                URI uri = getURI(file);
                element = new ArchiveElement.ArchiveElementBuilder()
                        .uri(uri)
                        .entryPath(
                                getEntryPath(
                                        uri, 
                                        baseDir, 
                                        replacementPath))
                        .size(Files.size(Paths.get(uri)))
                        .build();
            }
            catch (IllegalStateException ise) {
                LOGGER.warn("IllegalStateException raised while "
                        + "converting file to ArchiveElement.  Exception "
                        + "message => [ "
                        + ise.getMessage()
                        + " ].");
            }
            catch (FileNotFoundException fnfe) {
                LOGGER.warn("Target file [ "
                        + file
                        + " ] does not exist on the file system.  Exception "
                        + "message => [ "
                        + fnfe.getMessage()
                        + " ].");
            }
            catch (IOException ioe) {
                LOGGER.error("Unable to access target file [ " 
                        + file 
                        + " ] to obtain size data.  Exception message => [ "
                        + ioe.getMessage()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("Input file String is null or undefined.  Return "
                    + "value will be null.");
        }
        return element;
    }
    
    /**
     * Generate a single ArchiveElement object from an input file that was 
     * derived from a directory traversal. 
     * 
     * @param file The target file for bundling.
     * @param baseDir The parent base-directory.
     * @param replacementPath Path that will be used to replace the target 
     * base directory.
     * @return An associated ArchiveElement object.
     */
    public ArchiveElement getArchiveElement(
            URI    uri, 
            String baseDir, 
            String replacementPath) {
        
        ArchiveElement element = null;
        
        if (uri != null) {
            try {
                element = new ArchiveElement.ArchiveElementBuilder()
                        .uri(uri)
                        .entryPath(
                                getEntryPath(
                                        uri, 
                                        baseDir, 
                                        replacementPath))
                        .size(Files.size(Paths.get(uri)))
                        .build();
            }
            catch (IllegalStateException ise) {
                LOGGER.warn("IllegalStateException raised while "
                        + "converting file to ArchiveElement.  Exception "
                        + "message => [ "
                        + ise.getMessage()
                        + " ].");
            }
            catch (FileNotFoundException fnfe) {
                LOGGER.warn("Target file [ "
                        + uri.toString()
                        + " ] does not exist on the file system.  Exception "
                        + "message => [ "
                        + fnfe.getMessage()
                        + " ].");
            }
            catch (IOException ioe) {
                LOGGER.error("Unable to access target file [ " 
                        + uri.toString() 
                        + " ] to obtain size data.  Exception message => [ "
                        + ioe.getMessage()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("Input file String is null or undefined.  Return "
                    + "value will be null.");
        }
        return element;
    }
    
    /**
     * Generate a single ArchiveElement object from an input file.
     * 
     * @param file The target file for bundling.
     * @return An associated ArchiveElement object.
     */
    public ArchiveElement getArchiveElement(String file) {
        ArchiveElement element = null;
        if ((file != null) && (!file.isEmpty())) {
            try {
                URI uri = getURI(file);
                element = new ArchiveElement.ArchiveElementBuilder()
                            .uri(uri)
                            .entryPath(getEntryPath(uri))
                            .size(Files.size(Paths.get(uri)))
                            .build();
            }
            catch (IllegalStateException ise) {
                LOGGER.warn("IllegalStateException raised while "
                        + "converting file to ArchiveElement.  Exception "
                        + "message => [ "
                        + ise.getMessage()
                        + " ].");
            }
            catch (FileNotFoundException fnfe) {
                LOGGER.warn("Target file [ "
                        + file
                        + " ] does not exist on the file system.  Exception "
                        + "message => [ "
                        + fnfe.getMessage()
                        + " ].");
            }
            catch (IOException ioe) {
                LOGGER.error("Unable to access target file [ " 
                        + file 
                        + " ] to obtain size data.  Exception message => [ "
                        + ioe.getMessage()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("Input file String is null or undefined.  Return "
                    + "value will be null.");
        }
        return element;
    }
    
    /**
     * Generate a single ArchiveElement object from an input file that was 
     * derived from a directory traversal. 
     * 
     * @param file The target file for bundling.
     * @param baseDir The parent base-directory.
     * @param replacementPath Path that will be used to replace the target 
     * base directory.
     * @return An associated ArchiveElement object.
     */
    public ArchiveElement getArchiveElement(
            String file, 
            String replacementPath) {
        
        ArchiveElement element = null;
        
        if ((file != null) && (!file.isEmpty())) {
            try {
                URI uri = getURI(file);
                element = new ArchiveElement.ArchiveElementBuilder()
                        .uri(uri)
                        .entryPath(
                                getEntryPath(
                                        uri, 
                                        replacementPath))
                        .size(Files.size(Paths.get(uri)))
                        .build();
            }
            catch (IllegalStateException ise) {
                LOGGER.warn("IllegalStateException raised while "
                        + "converting file to ArchiveElement.  Exception "
                        + "message => [ "
                        + ise.getMessage()
                        + " ].");
            }
            catch (FileNotFoundException fnfe) {
                LOGGER.warn("Target file [ "
                        + file
                        + " ] does not exist on the file system.  Exception "
                        + "message => [ "
                        + fnfe.getMessage()
                        + " ].");
            }
            catch (IOException ioe) {
                LOGGER.error("Unable to access target file [ " 
                        + file 
                        + " ] to obtain size data.  Exception message => [ "
                        + ioe.getMessage()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("Input file String is null or undefined.  Return "
                    + "value will be null.");
        }
        return element;
    }
    
    
    /**
     * Convert a list of String path names into a List of ArchiveElement POJOs 
     * for submission to the bundler code.  This method signature was introduced 
     * to handle file lists that resulted from flattening of a user-supplied 
     * directory.
     * 
     * @param files An input list of Strings representing the full path to a
     * target file for archive/compression.
     * @param replacementPath User-specified string that will be pre-pended to the
     * entry path.  May be null or empty.
     * @return A list of ArchiveElement objects for submission to the bundler
     * methods.  The returned list may be empty, but will not be null.
     */
    public List<ArchiveElement> getArchiveElements(
            List<String> files,
            String       baseDir,
            String       replacementPath) {
        
        List<ArchiveElement> elements = new ArrayList<ArchiveElement>();
        
        if ((files != null) && (files.size() > 0)) {
            for (String file : files) {
                try {
                    ArchiveElement elem = getArchiveElement(
                                            file, 
                                            baseDir, 
                                            replacementPath);
                    if (elem != null) {
                        elements.add(elem);
                    }
                    else {
                        LOGGER.warn("Unable to generate an ArchiveElement "
                                + "object for file [ "
                                + file
                                + " ].");
                    }
                }
                catch (IllegalStateException ise) {
                    LOGGER.warn("IllegalStateException raised while "
                            + "converting file to ArchiveElement.  Exception "
                            + "message => [ "
                            + ise.getMessage()
                            + " ].");
                }
                catch (FileSystemNotFoundException fsnfe) {
                    LOGGER.warn("System error.  No file system provider "
                            + "available for the input URI scheme.  Exception "
                            + "message => [ "
                            + fsnfe.getMessage()
                            + " ].");
                }
            }
        }
        return elements;
    }
    
    
    /**
     * Convert a list of String path names into a List of ArchiveElement POJOs 
     * for submission to the bundler code.  This method signature was introduced 
     * to handle file lists that resulted from flattening of a user-supplied 
     * directory.
     * 
     * @param files An input list of Strings representing the full path to a
     * target file for archive/compression.
     * @param replacementPath User-specified string that will be pre-pended to the
     * entry path.  May be null or empty.
     * @return A list of ArchiveElement objects for submission to the bundler
     * methods.  The returned list may be empty, but will not be null.
     */
    public List<ArchiveElement> getURIArchiveElements(
            List<URI> files,
            String    baseDir,
            String    replacementPath) {
        
        List<ArchiveElement> elements = new ArrayList<ArchiveElement>();
        
        if ((files != null) && (files.size() > 0)) {
            for (URI file : files) {
                try {
                    ArchiveElement elem = getArchiveElement(
                                            file, 
                                            baseDir, 
                                            replacementPath);
                    if (elem != null) {
                        elements.add(elem);
                    }
                    else {
                        LOGGER.warn("Unable to generate an ArchiveElement "
                                + "object for file [ "
                                + file
                                + " ].");
                    }
                }
                catch (IllegalStateException ise) {
                    LOGGER.warn("IllegalStateException raised while "
                            + "converting file to ArchiveElement.  Exception "
                            + "message => [ "
                            + ise.getMessage()
                            + " ].");
                }
                catch (FileSystemNotFoundException fsnfe) {
                    LOGGER.warn("System error.  No file system provider "
                            + "available for the input URI scheme.  Exception "
                            + "message => [ "
                            + fsnfe.getMessage()
                            + " ].");
                }
            }
        }
        return elements;
    }
    
    
    /**
     * Convert a list of String path names into a List of ArchiveElement POJOs 
     * for submission to the bundler code.  
     * 
     * @param files An input list of Strings representing the full path to a
     * target file for archive/compression.
     * @return A list of ArchiveElement objects for submission to the bundler
     * methods.  The returned list may be empty, but will not be null.
     */
    public List<ArchiveElement> getArchiveElements(List<String> files) {
        List<ArchiveElement> elements = new ArrayList<ArchiveElement>();
        if ((files != null) && (files.size() > 0)) {
            for (String file : files) {
                try {
                    ArchiveElement elem = getArchiveElement(file);
                    if (elem != null) {
                        elements.add(elem);
                    }
                    else {
                        LOGGER.warn("Unable to generate an ArchiveElement "
                                + "object for file [ "
                                + file
                                + " ].");
                    }
                }
                catch (IllegalStateException ise) {
                    LOGGER.warn("IllegalStateException raised while "
                            + "converting file to ArchiveElement.  Exception "
                            + "message => [ "
                            + ise.getMessage()
                            + " ].");
                }
                catch (FileSystemNotFoundException fsnfe) {
                    LOGGER.warn("System error.  No file system provider "
                            + "available for the input URI scheme.  Exception "
                            + "message => [ "
                            + fsnfe.getMessage()
                            + " ].");
                }
            }
        }
        return elements;
    }
    
    /**
     * Method generates a list of files that fall below the input URI.  It is 
     * intended that the input URI will identify a directory.  If the input
     * URI is a regular file, that URI will be returned. 
     * 
     * @param uri URI of a directory.
     * @return List of files that fall below the input directory.  The return 
     * may be empty, but will not be null.
     */
    private List<URI> getFileList(URI uri) {
        
        List<URI> files = new ArrayList<URI>();
        
        if (uri != null) {
            try {
                List<URI> uris = FileFinder.listFiles(uri);
                if ((uris != null) && (uris.size() > 0)) { 
                    for (URI element : uris) {
                        if (!Files.isDirectory(Paths.get(element))) {
                            files.add(element);
                        }
                    }
                }
            }
            catch (IOException ioe) {
                LOGGER.error("Unexpected IOException raised while obtaining a "
                        + "listing of URI [ "
                        + uri.toString()
                        + " ].  Exception message => [ "
                        + ioe.getMessage()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("The input URI is null.  An empty list will be returned.");
        }
        return files;
    }
    
    public List<ArchiveElement> getArchiveElements(FileRequest request) {
    	List<ArchiveElement> elements = new ArrayList<ArchiveElement>();
    	if (request != null) {
	        if ((request.getFile() != null) && (!request.getFile().isEmpty())) { 
	            try {
	                URI uri = getURI(request.getFile());
	                Path p = Paths.get(uri);
	                if (Files.isDirectory(p)) {
	                    elements.addAll(
	                            getURIArchiveElements(
	                                    getFileList(uri),
	                                    p.toString(),
	                                    request.getArchivePath()));
	                }
	                else {
	                    if (request.getArchivePath() != null) {
	                        elements.add(getArchiveElement(request.getFile(), request.getArchivePath()));
	                    }
	                    else {
	                        elements.add(getArchiveElement(request.getFile()));
	                    }
	                }
	            }
	            catch (FileNotFoundException fnfe) {
	                LOGGER.warn("Target directory [ "
	                        + request.getFile()
	                        + " ] does not exist on the file system.  Exception "
	                        + "message => [ "
	                        + fnfe.getMessage()
	                        + " ].");
	            }
	            catch (IOException ioe) {
	                LOGGER.error("Unexpected IOException raised while attempting "
	                        + "to walk the file tree for directory [ "
	                        + request.getFile()
	                        + " ].  Exception message => [ "
	                        + ioe.getMessage());
	            }
	        }
    	}
    	else {
    		
    	}
        return elements;   
    	
    }
    /**
     * This method signature accepts a single <code>String</code> filename 
     * that represents a directory.  The code then expands the contents of that
     * directory generating the associated list of files. 
     * 
     * @param file String-based file object.  If it doesn't represent a 
     * directory a list containing a single ArchiveElement object will be 
     * returned.
     * @param replacementPath Client-supplied replacement path.  If the 
     * replacement path is an empty String, the output will be just the file
     * name.  If the replacement Path is null, the usual rules for generating
     * an entry path will be followed.
     * @return List of ArchiveElement objects.
     */
    public List<ArchiveElement> getArchiveElements(
            String file, 
            String replacementPath) {
        List<ArchiveElement> elements = new ArrayList<ArchiveElement>();
        if ((file != null) && (!file.isEmpty())) { 
            try {
                URI uri = getURI(file);
                Path p = Paths.get(uri);
                if (Files.isDirectory(p)) {
                    elements.addAll(
                            getURIArchiveElements(
                                    getFileList(uri),
                                    p.toString(),
                                    replacementPath));
                }
                else {
                    if (replacementPath != null) {
                        elements.add(getArchiveElement(file, replacementPath));
                    }
                    else {
                        elements.add(getArchiveElement(file));
                    }
                }
            }
            catch (FileNotFoundException fnfe) {
                LOGGER.warn("Target directory [ "
                        + file
                        + " ] does not exist on the file system.  Exception "
                        + "message => [ "
                        + fnfe.getMessage()
                        + " ].");
            }
            catch (IOException ioe) {
                LOGGER.error("Unexpected IOException raised while attempting "
                        + "to walk the file tree for directory [ "
                        + file
                        + " ].  Exception message => [ "
                        + ioe.getMessage());
            }
        }
        return elements;    
    }
    
    public static void main (String[] args) {
        
        List<String> fileList = new ArrayList<String>();
        fileList.add("file:///tmp/test_file_1");
        fileList.add("/tmp/test_file_2");
        fileList.add("file:///mnt/fbga/CDRG/cdrgxgdneur50kc_1/covdata/ctlm50.cov");
        // Must add s3fs filesystem for the following to work.
        fileList.add("s3fs:///tmp/test_file_3");
        List<ArchiveElement> outputList = (new ArchiveElementFactory()).getArchiveElements(fileList);
        for (ArchiveElement element : outputList) {
            System.out.println(element.toString());
        }
        
        List<String> flatFileList = new ArrayList<String>();
        
        flatFileList.add("file:///tmp/test/test2/test_file_2");
        flatFileList.add("file:///tmp/test/test2/test_file_1");
        flatFileList.add("file:/tmp/test/test2/test_file_3");
        flatFileList.add("file:/tmp/test/test2/test_file_4");
        List<ArchiveElement> outputList2 = 
                (new ArchiveElementFactory()).getArchiveElements(flatFileList, "/tmp/test", "/new_root");
        for (ArchiveElement element : outputList2) {
            System.out.println(element.toString());
        }
        
        String directory = "/tmp";
        List<ArchiveElement> directoryListing = 
                (new ArchiveElementFactory()).getArchiveElements(directory, "blah/blah");
        for (ArchiveElement element : directoryListing) {
            System.out.println(element.toString());
        }
        
        String test = "file:///tmp/test/test2/test_file_2";
        List<ArchiveElement> singleFile = 
                (new ArchiveElementFactory()).getArchiveElements(test, "blah/blah");
        for (ArchiveElement element : singleFile) {
            System.out.println(element.toString());
        }
        
        String testNoPathReplacement = "file:///tmp/test/test2/test_file_2";
        List<ArchiveElement> singleFileNoPathReplacement = 
                (new ArchiveElementFactory()).getArchiveElements(testNoPathReplacement, "");
        for (ArchiveElement element : singleFileNoPathReplacement) {
            System.out.println(element.toString());
        }
        String testNoPathReplacement2 = "file:///tmp/test/test2/test_file_2";
        List<ArchiveElement> singleFileNoPathReplacement2 = 
                (new ArchiveElementFactory()).getArchiveElements(testNoPathReplacement2, null);
        for (ArchiveElement element : singleFileNoPathReplacement2) {
            System.out.println(element.toString());
        }
        
        try {
        	URI uri = (new ArchiveElementFactory()).getURI("/tmp/test");
        	System.out.println("URI : " + uri.toString());
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        
    }
    
}
