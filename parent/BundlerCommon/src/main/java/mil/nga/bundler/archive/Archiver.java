package mil.nga.bundler.archive;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.bundler.interfaces.FileCompletionListenerI;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.bundler.types.ArchiveType;

/**
 * This is class was designed to encapsulate the logic used for creating the 
 * output archive files.
 * 
 * @author carpenlc
 */
public abstract class Archiver {
    
    /**
     * Default name to use if the output file is not supplied.
     */
    public static final String DEFAULT_ARCHIVE_FILENAME = "archive";
    
    /**
     * Set up the Log4j system for use throughout the class
     */        
    final static Logger LOGGER = LoggerFactory.getLogger(Archiver.class);
    
    /**
     * URI defining the output file to construct.
     */
    protected URI outputFile = null;
    
    /**
     * List of listeners that have registered to be notified when individual files 
     * have completed processing.
     */
    private List<FileCompletionListenerI> listeners;
    
    /**
     * Used for thread-safety.
     */
    private Object MUTEX = new Object();
    
    /**
     * Default constructor.
     */
    public Archiver() { }
    
    /**
     * Add a listener for file completion.  This listener is used for updating the 
     * handling any status data associated with the completion of processing associated
     * with a given file.
     * 
     * @param listener Listener to be notified when a file completes update processing.
     */
    public void addFileCompletionListener(FileCompletionListenerI listener) {
        if (listener != null) {
            if (listeners == null) {
                listeners = new ArrayList<FileCompletionListenerI>();
            }
            synchronized (MUTEX) {
                if (!listeners.contains(listener)) {
                    listeners.add(listener);
                }
            }
        }
    }
    
    /**
     * This method will copy the contents of the file identified by 
     * the input URL into the input output stream object.
     * 
     * @param out The target archive output stream.
     * @param file The file to copy.
     */
    public void copyOneFile(ArchiveOutputStream out, URI file) {
        if (file != null) {
            if (out != null) {
                Path p = Paths.get(file);
                try {
                    Files.copy(p, out);
                    out.closeArchiveEntry();
                }
                catch (IOException ioe) {
                    LOGGER.error("Unexpected IOException encountered while "
                            + "copying file [ "
                            + file.toString()
                            + " ].  To the archive output stream.  Exception "
                            + "message => [ "
                            + ioe.getMessage()
                            + " ].");
                }
            }
            else { 
                LOGGER.error("Client supplied OutputStream is null.  Copy " 
                        + "into archive will not occur.");
            }
        }
        else {
            LOGGER.error("Input file URI is null.  Nothing to copy.");
        }
    }
    
    
    /**
     * This method is part of the implementation of the Observer design 
     * pattern. This allows users of classes extending from Archiver to 
     * be notified when processing associated with a given file are 
     * complete.
     * 
     * @param value The <code>ArchiveElement</code> object that has changed
     * it's internal state.
     */
    public void notify(ArchiveElement value) {
        if ((listeners != null) && (listeners.size() > 0)) {
            List<FileCompletionListenerI> localListeners = null;
            synchronized(MUTEX) {
                localListeners = new ArrayList<FileCompletionListenerI>(listeners);
            }
            for (FileCompletionListenerI listener : localListeners) {
                listener.notify(value);
            }
         }
        else {
            LOGGER.info("Archive of file => [ "
                    + value.toString() 
                    + " ] complete.");
        }
    }
    
    /**
     * 
     * @return The full URI of the target output file.
     */
    public URI getOutputFile() {
        if (outputFile == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Output archive file not specified.  "
                        + "Generating a default file name.");
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("file://");
            sb.append(System.getProperty("java.io.tmpdir"));
            if (!sb.toString().endsWith("/")) {
                sb.append("/");
            }
            sb.append(DEFAULT_ARCHIVE_FILENAME);
            sb.append("_");
            sb.append(System.nanoTime());
            sb.append(".");
            sb.append(getArchiveType().getText());
            outputFile = URI.create(sb.toString());
        }
        
        return outputFile;
    }
    
    /**
     * Setter method for the full URI of the output file.  This method will 
     * ensure that the output file has the correct file extension.
     * 
     * @param value The URI of the output file.
     */
    public void setOutputFile(URI value) {
        if (value != null) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(value.getPath());
                if (!sb.toString().endsWith(getArchiveType().getText())) {
                    if (!sb.toString().endsWith(".")) {
                        sb.append(".");
                    }
                    sb.append(getArchiveType().getText());
                }
                outputFile = new URI(
                        value.getScheme(), 
                        value.getAuthority(),  
                        sb.toString(), 
                        value.getQuery(), 
                        value.getFragment());
            }
            // We're creating a URI from an existing URI so we should never 
            // get this exception.
            catch (URISyntaxException use) { }
        }
    }
    
    /**
     * Setter method for the full URI of the output file.  This method will 
     * ensure that the output file has the correct file extension.
     * 
     * @param value The URI of the output file.
     * @param type The type of output file that is going to be created.
     */
    public void setOutputFile(URI value, String type) {
        if (value != null) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(value.getPath());
                if (!sb.toString().endsWith(type)) {
                    if (!sb.toString().endsWith(".")) {
                        sb.append(".");
                    }
                    sb.append(type);
                }
                outputFile = new URI(
                        value.getScheme(), 
                        value.getAuthority(), 
                        sb.toString(), 
                        value.getQuery(), 
                        value.getFragment());
            }
            // We're creating a URI from an existing URI so we should never 
            // get this exception.
            catch (URISyntaxException use) { }
        }
    }
    
    /**
     * Subclasses must provide a mechanism for creating the appropriate 
     * object of type ArchiveEntry.
     * 
     * @param file The file that will be added to the Archive.
     * @param name The name (full path)
     * @return A concrete ArchiveEntry object (ZipArchiveEntry 
     * or TarArchiveEntry)
     */
    public abstract ArchiveEntry getArchiveEntry(URI file, String entryPath) throws IOException ;
    
    /**
     * Subclasses must provide a method identifying the type of archive that 
     * will be created.
     * 
     * @return The archive type.
     */
    public abstract ArchiveType getArchiveType();


}
