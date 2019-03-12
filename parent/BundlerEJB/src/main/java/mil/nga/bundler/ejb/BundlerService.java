package mil.nga.bundler.ejb;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.bundler.archive.ArchiveFactory;
import mil.nga.bundler.exceptions.ArchiveException;
import mil.nga.bundler.exceptions.ServiceUnavailableException;
import mil.nga.bundler.exceptions.UnknownArchiveTypeException;
import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.interfaces.BundlerI;
import mil.nga.bundler.messages.ArchiveMessage;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.bundler.model.ArchiveJob;
import mil.nga.bundler.model.FileEntry;
import mil.nga.bundler.types.JobStateType;
import mil.nga.util.FileUtils;
import mil.nga.util.URIUtils;

/**
 * Session Bean implementation class BundlerService
 */
@Stateless
@LocalBean
public class BundlerService 
        extends NotificationService implements BundlerConstantsI {

    /**
     * Set up the Log4j system for use throughout the class
     */
    static final Logger LOGGER = LoggerFactory.getLogger(BundlerService.class);
    
    /**
     * Maximum number of times to attempt to read the archive data from the 
     * backing data source.
     */
    private static final int MAX_ATTEMPTS = 5;
    
    /**
     * The amount of times to wait between database read attempts.
     */
    private static final long WAIT_TIME = 5000;
    
    /**
     * Container-injected reference to the JobService EJB.
     */
    @EJB
    ArchiveJobService archiveJobService;
    
    /**
     * Container-injected reference to the HashGenerator service.
     */
    @EJB
    HashGeneratorService hashGeneratorService;
    
    /**
     * Container-injected reference to the FileCompletionListener service.
     */
    @EJB
    FileCompletionListener fileCompletionlistener;
    
    /**
     * Default constructor. 
     */
    public BundlerService() { }

    /**
     * Method introduced to attempt to work around some latency issues with 
     * JPA flushing data to the backing data store for use by other nodes in 
     * the cluster.
     * 
     * @param jobID The job ID to process.
     * @param archiveID The ID of the archive to process.
     * 
     * @return The <code>ArchiveJob</code> to process.   May be null if the 
     * data is not available.
     */
    private ArchiveJob getArchiveJob(String jobID, long archiveID) 
    		throws ServiceUnavailableException {
    	
    	int        counter = 0;
    	ArchiveJob archive = getArchiveJobService()
        		.getArchiveJob(jobID, archiveID);
        
        // We have run into situations where the JPA subsystem has not 
        // flushed all of the data out to the backing data store at this 
        // point in processing.  Additional logic has been inserted 
        // here to perform multiple attempts before failing the job.
        if (archive == null) {
        	while ((counter < MAX_ATTEMPTS) && (archive == null)) {
        		LOGGER.info("Unable to find archive to process for "
                        + "job ID [ "
                        + jobID
                        + " ] and archive ID [ "
                        + archiveID
                        + " ].  Attempt number [ "
                        + (counter + 1)
                        + " ] out of a maximum of [ "
                        + MAX_ATTEMPTS
                        + " ] attempts.");
        		try {
					Thread.sleep(WAIT_TIME);
        		}
        		catch (InterruptedException ie) {
        			LOGGER.debug("Unexpected InterruptedException raised "
        					+ "while pausing execution.  Exception "
        					+ "=> [ "
        					+ ie.getMessage()
        					+ " ].");
        		}
        		archive = getArchiveJobService()
                		.getArchiveJob(jobID, archiveID);
        		counter++;
        	}
        }
        return archive;
    }
    
    /**
     * Method driving the creation of the output archive file.
     * 
     * @param job The managed JPA job object.
     * @param archive Archive job to run.
     */
    private void createArchive(String jobID, long archiveID) 
            throws ArchiveException, IOException { 
        
        long startTime = System.currentTimeMillis();
        
        try {
            
            ArchiveJob archive = getArchiveJob(jobID, archiveID);
            
            if (archive != null) {
                
                // Get the concrete instance of the archiver that will be
                // used to construct the output archive file.
                ArchiveFactory factory = ArchiveFactory.getInstance();
            
                // Get the concrete Bundler object.
                BundlerI bundler = factory.getBundler(archive.getArchiveType());
              
                // Set up the listener for the completion of individual file 
                // archives.  This was added at the request of the MPSU team and 
                // may need to be removed if too much of an impact to 
                // performance.
                FileCompletionListener listener = getFileCompletionListener();
                if (listener != null) {
                    listener.setJobID(jobID);
                    listener.setArchiveID(archiveID);
                    bundler.addFileCompletionListener(listener);
                }
                
                // Here's where the magic happens.
                bundler.bundle(
                        getArchiveElements(archive.getFiles()), 
                        URIUtils.getInstance().getURI(archive.getArchive()));
               
                // Generate the hash file associated with the output archive.
                if (getHashGeneratorService() != null) {
                    getHashGeneratorService().generate(
                            archive.getArchive(),
                            archive.getHash());
                }
                else {
                    LOGGER.warn("Unable to obtain a reference to the "
                            + "HashGenerator EJB.  Unable to create the output "
                            + "hash file associated with job ID [ "
                            + archive.getJobID()
                            + " ] and archive ID [ "
                            + archiveID
                            + " ].  Since few, if any, customers actually use "
                            + "the hash for anything we just issue a warning "
                            + "and proceed with processing.");
                }
            
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Archive processing for job ID [ "
                            + jobID
                            + " ] and archive ID [ "
                            + archiveID
                            + " ].  Completed in [ "
                            + (System.currentTimeMillis() - startTime)
                            + " ] ms.");
                }
            }
            else {
                LOGGER.error("Unable to find archive to process for "
                            + "job ID [ "
                            + jobID
                            + " ] and archive ID [ "
                            + archiveID
                            + " ].  The maximum number of tries [ "
                            + MAX_ATTEMPTS 
                            + " ] were exceeded.");
            }
        
        }
        catch (ServiceUnavailableException sue) {
            LOGGER.error("Internal system failure.  Target EJB service "
                    + "is unavailable.  Exception message => [ "
                    + sue.getMessage()
                    + " ].");
        }
        catch (UnknownArchiveTypeException uate) {
            // We should never see this exception here.  However, we will log 
            // it as there must a programming error.
            LOGGER.error("Unexpected UnknownArchiveException raised while "
                    + "actually creating the output archive.  This sitation "
                    + "should have been caught much earlier than here.  "
                    + "Exception message => [ "
                    + uate.getMessage()
                    + " ].");
        }   
    }
   
    /**
     * Map the input list of <code>FileEntry</code> objects to an output list of 
     * <code>ArchiveElement</code> objects to pass into the bundler algorithm.
     *  
     * @param files A list of <code>FileEntry</code> objects to bundle.
     * @return a list containing <code>ArchiveElement</code> objects.  The 
     * output may be empty, but it will not be null.
     */
    public List<ArchiveElement> getArchiveElements(List<FileEntry> files) {
        List<ArchiveElement> elements = new ArrayList<ArchiveElement>();
        if ((files != null) && (files.size() > 0)) {
            for (FileEntry file : files) {
                elements.add(new ArchiveElement.ArchiveElementBuilder()
                                    .size(file.getSize())
                                    .entryPath(file.getEntryPath())
                                    .uri(URIUtils.getInstance()
                                            .getURI(file.getFilePath()))
                                    .build());
            }
        }
        else {
            LOGGER.warn("Input list of FileEntry objects is null or empty.  "
                    + "Output list will also be empty.");
        }
        return elements;
    }
    
    /**
     * Private method used to obtain a reference to the target EJB.  
     * 
     * Method implemented because JBoss EAP 6.x was inexplicably NOT always
     * injecting the EJB (i.e. EJB reference was null)
     * 
     * @return Reference to the FileCompletionListener EJB.
     */
    private FileCompletionListener getFileCompletionListener() {
        if (fileCompletionlistener == null) {
            LOGGER.warn("Application container failed to inject the "
                    + "reference to FileCompletionListener.  Attempting to "
                    + "look it up via JNDI.");
            fileCompletionlistener = EJBClientUtilities
                    .getInstance()
                    .getFileCompletionListener();
        }
        return fileCompletionlistener;
    }
    
    /**
     * Private method used to obtain a reference to the target EJB.  
     * 
     * Method implemented because JBoss EAP 6.x was inexplicably NOT always
     * injecting the EJB (i.e. EJB reference was null)
     * 
     * @return Reference to the HashGeneratorService EJB.
     * @throws ServiceUnavailableException Thrown if we are unable to obtain
     * a reference to the target EJB.
     */
    private HashGeneratorService getHashGeneratorService() 
            throws ServiceUnavailableException {
        if (hashGeneratorService == null) {
            LOGGER.warn("Application container failed to inject the "
                    + "reference to HashGeneratorService.  Attempting to "
                    + "look it up via JNDI.");
            hashGeneratorService = EJBClientUtilities
                    .getInstance()
                    .getHashGeneratorService();
            if (hashGeneratorService == null) {
                throw new ServiceUnavailableException("Unable to obtain a "
                        + "reference to [ "
                        + HashGeneratorService.class.getCanonicalName()
                        + " ].");
            }
        }
        return hashGeneratorService;
    }
    
    /**
     * Private method used to obtain a reference to the target EJB.  
     * 
     * Method implemented because JBoss EAP 6.x was inexplicably NOT always
     * injecting the EJB (i.e. EJB reference was null)
     * 
     * @return Reference to the JobService EJB.
     * @throws ServiceUnavailableException Thrown if we are unable to obtain
     * a reference to the target EJB.
     */
    private ArchiveJobService getArchiveJobService() 
            throws ServiceUnavailableException {
        
        if (archiveJobService == null) {
            LOGGER.warn("Application container failed to inject the "
                    + "reference to ArchiveJobService.  Attempting to "
                    + "look it up via JNDI.");
            archiveJobService = EJBClientUtilities
                    .getInstance()
                    .getArchiveJobService();
            if (archiveJobService == null) {
                throw new ServiceUnavailableException("Unable to obtain a "
                        + "reference to [ "
                        + JobFactoryService.class.getCanonicalName()
                        + " ].");
            }
        }
        return archiveJobService;
    }
    
    /**
     * 
     * Note: This method was added to handle very large bundle requests.
     * We found that if the bundle process took longer than 5 minutes the 
     * JMS system would re-issue the message.
     * 
     * @param message Message indicating which Job ID/Archive ID to process.
     */
    @Asynchronous
    public void handleMessage(ArchiveMessage message) {
        
        JobStateType endState;
        int  counter   = 0;
        
        try {
            
            ArchiveJob archiveJob = getArchiveJob(
                    message.getJobId(), 
                    message.getArchiveId());
               
            if (archiveJob != null) {
                    
                // Update the archive to reflect that archive processing 
                // has started.
                archiveJob.setHostName(FileUtils.getHostName());
                archiveJob.setServerName(
                                EJBClientUtilities.getInstance().getServerName());
                archiveJob.setStartTime(System.currentTimeMillis());
                archiveJob.setArchiveState(JobStateType.IN_PROGRESS);    
                        
                getArchiveJobService().update(archiveJob);
                        
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Creating output archive file for "
                            + "job ID [ "
                            + archiveJob.getJobID()
                            + " ] and archive ID [ "
                            + archiveJob.getArchiveID()
                            + " ].");
                }
                  
                try {
                    createArchive(message.getJobId(), message.getArchiveId());
                    endState = JobStateType.COMPLETE;
                }
                catch (IOException ioe) {
                    LOGGER.error("Unexpected IOException raised while "
                            + "creating the output archive.  Archive "
                            + "state will be set to ERROR for job ID [ "
                            + message.getJobId()
                            + " ] archive ID [ "
                            + message.getArchiveId()
                            + " ].  Error message [ "
                            + ioe.getMessage()
                            + " ].");
                    endState = JobStateType.ERROR;
                }
                catch (ArchiveException ae) {
                    LOGGER.error("Unexpected ArchiveException raised "
                            + "while "
                            + "creating the output archive.  Archive "
                            + "state will be set to ERROR for job ID [ "
                            + message.getJobId()
                            + " ] archive ID [ "
                            + message.getArchiveId()
                            + " ].  Error message [ "
                            + ae.getMessage()
                            + " ].");
                    endState = JobStateType.ERROR;
                }
                        
                // The status of the ARCHIVE_JOB has changed due to the
                // implementation of the FileCompletionListener.  Go get 
                // the latest ARCHIVE_JOB from the data store.
                archiveJob = getArchiveJobService().getArchiveJob(
                        message.getJobId(), 
                        message.getArchiveId());
                
                if (archiveJob != null) {
                    archiveJob.setArchiveState(endState);
                    
                    // Update the end time.
                    archiveJob.setEndTime(System.currentTimeMillis());
                    
                    // Go get the final size of the output archive.
                    archiveJob.setSize(getArchiveFileSize(
                                            archiveJob.getArchive()));
                            
                    // Ensure the ArchiveJob is updated in the backing data store.
                    getArchiveJobService().update(archiveJob);
            
                }
                else {
                    LOGGER.error("Unable to retrieve the ArchiveJob object "
                            + "for job ID [ "
                            + message.getJobId()
                            + " ] and archive ID [ "
                            + message.getArchiveId()
                            + " ] from the data store.  Archive job status will "
                            + "not be updated here.  Will attempt to update "
                            + "the status on notification.");
                }
            
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Archive complete.  Sending " 
                            + "notification message file for "
                            + "archive with job ID [ "
                            + message.getJobId()
                            + " ] and archive ID [ "
                            + message.getArchiveId()
                            + " ].");
                }
                
                notify(message.getJobId(), message.getArchiveId());
            }
            else {
                LOGGER.error("Unable to find an ARCHIVE_JOB matching "
                        + "archive message parameters => [ "
                        + message.toString()
                        + " ].");
            }
        }
        
        catch (ServiceUnavailableException sue) {
            LOGGER.error("Internal system failure.  Target EJB service "
                    + "is unavailable.  Exception message => [ "
                    + sue.getMessage()
                    + " ].");
        }
    }
    
    /**
     * Simple method used to retrieve the size of the created archive file.
     * 
     * @param archive The completed Archive object.
     */
    private long getArchiveFileSize(String archive) {
        
        long size = 0L;
        
        if ((archive != null) && (!archive.isEmpty())) {
            URI output = URIUtils.getInstance().getURI(archive);
            
            Path p = Paths.get(output);
            if (Files.exists(p)) {
                try {
                    size = Files.size(p);
                }
                catch (IOException ioe) {
                    LOGGER.error("Unexpected IOException while attempting "
                            + "to obtain the size associated with file [ "
                            + output.toString()
                            + " ].  Exception message => [ "
                            + ioe.getMessage()
                            + " ].");
                }
            }
            else {
                LOGGER.error("The expected output archive file [ "
                        + archive
                        + " ] does not exist.");
            }
        }
        else {
            LOGGER.error("The identified output archive file is null or "
                    + "empty.  The final output archive size will not be "
                    + "set.");
        }
        return size;
    }
    
    /**
     * This method is used to notify the Tracker MDB that the processing 
     * associated with a single Archive has completed.  The JPA Archive 
     * object is wrapped in an ObjectMessage and then placed on the 
     * appropriate JMS Queue.
     * 
     * @param archive The JPA Archive containing information associated with
     * the output files created.
     */
    private void notify(String jobID, long archiveID) {
        
        ArchiveMessage archiveMsg = new ArchiveMessage.ArchiveMessageBuilder()
                .jobId(jobID)
                .archiveId(archiveID)
                .build();
                
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Placing the following message on "
                    + "the JMS queue [ "
                    + archiveMsg.toString()
                    + " ].");
        }
        
        super.notify(TRACKER_DEST_Q, archiveMsg);
                
    }
    
}
