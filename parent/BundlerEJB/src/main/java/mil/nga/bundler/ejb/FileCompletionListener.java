package mil.nga.bundler.ejb;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.bundler.exceptions.ServiceUnavailableException;
import mil.nga.bundler.interfaces.FileCompletionListenerI;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.bundler.types.JobStateType;

/**
 * This class is follows the Observer design pattern.  It is registered 
 * as a listener with classes that extend from 
 * <code>mil.nga.bundler.archive.Archiver</code>.  The <code>Archiver</code>
 * classes invoke the <code>notify()</code> method when a single target 
 * file completes the archive/compression process.  
 * 
 * This logic was added at the request of the MPSU team who wanted better
 * real-time information on the state of a bundle job.  The old algorithm 
 * updated the state of each file in an archive when the entire archive 
 * process was complete.  
 * 
 * Note: Because we go back to the data store after each file, the addition 
 * of this logic slows down the bundle process.  If we need performance 
 * improvements, this is a good place to start.
 */
@Stateful
@LocalBean
public class FileCompletionListener 
		implements Serializable, FileCompletionListenerI {

	/**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		FileCompletionListener.class);
    
    /**
     * The job ID that this listener is associated with. 
     */
    private String jobID;
    
    /**
     * The archive ID that this listener is associated with.
     */
    private long archiveID;
    
    /**
     * Reference to FileEntryService session bean that will be used to update the 
     * back-end data store with the current job state.
     */
    @EJB
    FileEntryService fileEntryService;
    
    /**
     * Default constructor. 
     */
    public FileCompletionListener() { }
    
    /**
     * Private method used to obtain a reference to the target EJB.  
     * @return Reference to the FileEntryService EJB.
     */
    private FileEntryService getFileEntryService() 
    		throws ServiceUnavailableException {
        if (fileEntryService == null) {
            LOGGER.warn("Application container failed to inject the "
                    + "reference to FileEntryService.  Attempting to "
                    + "look it up via JNDI.");
            fileEntryService = EJBClientUtilities
                    .getInstance()
                    .getFileEntryService();
            if (fileEntryService == null) {
                throw new ServiceUnavailableException("Unable to obtain a "
                		+ "reference to [ "
                        + FileEntryService.class.getCanonicalName()
                        + " ].");
            }
        }
        return fileEntryService;
    }
    
    /**
     * Method satisfying the <code>FileCompletionListenerI</code> interface.  
     * It accepts and object of type <code>ArchiveElement</code> and updates 
     * the state of that file in the backing data store.  This method is 
     * called after the compression of the file has completed so the state is 
     * always set to <code>COMPLETE</code>.
     * 
     * @param element The file data to update.
     */
    @Override
    public void notify(ArchiveElement element) {
    	if (element != null) {
    		if (LOGGER.isDebugEnabled()) {
    	    	LOGGER.debug("Notify method called for job ID [ "
    	    			+ getJobID() 
    	    			+ " ], archive ID [ "
    	    			+ getArchiveID()
    	    			+ " ].  Element completed => [ "
    	    			+ element.toString()
    	    			+ " ].");
    		}
    		try {
	    		if (getFileEntryService() != null) {
	    			getFileEntryService().updateState(
	    					getJobID(),
	    					getArchiveID(),
	    					element.getURI().toString(),
	    					JobStateType.COMPLETE);
	    		}
    		}
    		catch (ServiceUnavailableException sue) {
            	LOGGER.error("Internal system failure.  Target EJB service "
            			+ "is unavailable.  Exception message => [ "
            			+ sue.getMessage()
            			+ " ].");
    		}
    	}
    }
    
    /**
     * Setter method for the archive ID that this listener is associated with.
     * @return value The archive ID.
     */
    public long getArchiveID() {
    	return archiveID;
    }
    
    /**
     * Getter method for the job ID that this listener is associated with.
     * @return value The job ID.
     */
    public String getJobID() {
    	return jobID;
    }
    
    /**
     * Setter method for the archive ID that this listener is associated with.
     * @param value The archive ID.
     */
    public void setArchiveID(long value) {
    	this.archiveID = value;
    }
    
    /**
     * Setter method for the job ID that this listener is associated with.
     * @param value The job ID.
     */
    public void setJobID(String value) {
    	this.jobID = value;
    }
}
