package mil.nga.bundler.ejb;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import mil.nga.bundler.exceptions.ServiceUnavailableException;
import mil.nga.bundler.messages.JobTrackerMessage;
import mil.nga.bundler.messages.JobTrackerMessage.JobTrackerMessageBuilder;
import mil.nga.bundler.model.ArchiveJob;
import mil.nga.bundler.model.FileEntry;
import mil.nga.bundler.model.Job;
import mil.nga.bundler.types.JobStateType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This bean is responsible for creating the data required to provide state 
 * information associated with a given bundle job.  The state data is returned
 * to callers via the web tier through the getState() call.  This differs 
 * from previous versions of the bundler because it calculates the state from 
 * the Job object rather than reading information from a separate table.
 */
@Stateless
@LocalBean
public class JobTrackerService {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
            JobTrackerService.class);
    
    /**
     * Container-injected reference to the JobService EJB.
     */
    @EJB
    JobService jobService;
    
    /**
     * Default constructor. 
     */
    public JobTrackerService() { }
    
    /**
     * Private method used to obtain a reference to the target EJB.  
     * 
     * @return Reference to the JobService EJB.
     */
    private JobService getJobService() throws ServiceUnavailableException {
        if (jobService == null) {
            LOGGER.warn("Application container failed to inject the "
                    + "reference to JobService.  Attempting to "
                    + "look it up via JNDI.");
            jobService = EJBClientUtilities
                    .getInstance()
                    .getJobService();
            if (jobService == null) {
            	throw new ServiceUnavailableException("Unable to look up "
                        + "target EJB [ "
            			+ JobService.class.getCanonicalName()
                        + " ].");
            }
        }
        return jobService;
    }
    
    /**
     * Calculate the elapsed time associated with the job.
     * 
     * @param startTime The time that the job was started.
     * @param endTime The time that the job completed (if available).
     * @return The amount of wall-clock time the job has taken.
     */
    private long getElapsedTime(long startTime, long endTime) {
        long elapsedTime = 0L;
        if ((endTime > 0) && (startTime > 0)) {
            elapsedTime = endTime - startTime;
        }
        if ((endTime == 0) && (startTime > 0)) { 
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        return elapsedTime;
    }

    /**
     * Generate the current  statistics associated with the input Job object.
     * 
     * @param job Target Job requested.
     * @param builder The JobTrackerMessageBuilder Object used to create the 
     * JobTrackerMessage object.
     */
    private void getJobTrackerMessage(Job job, JobTrackerMessageBuilder builder) {
    	
    	int  numArchivesComplete = 0;
        long numFilesComplete    = 0L;
        long totalSizeComplete   = 0L;
        
    	if (job != null) {
    		
    		// Copy data from the Job object into the JobTrackerMessageBuilder
    		builder.jobID(job.getJobID());
            builder.userName(job.getUserName());
            builder.numFiles(job.getNumFiles());
            builder.totalSize(job.getTotalSize());
            builder.numArchives(job.getNumArchives());
            builder.state(job.getState());
            builder.elapsedTime(getElapsedTime(job.getStartTime(), job.getEndTime()));
          
            // Calculate the remaining fields
            if ((job.getArchives() != null) && (job.getArchives().size() > 0)) {
            	
            	for (ArchiveJob archive : job.getArchives()) {
            		if (archive.getArchiveState() == JobStateType.COMPLETE) {
                        numArchivesComplete++;
                        builder.archive(archive);
                    }
                    if ((archive.getFiles() != null) && 
                            (archive.getFiles().size() > 0)) {
                        for (FileEntry file : archive.getFiles()) {
                            if (file.getFileState() == JobStateType.COMPLETE) {
                                numFilesComplete++;
                                totalSizeComplete += file.getSize();
                            }
                        }
                    }
                    else {
                        LOGGER.warn("Job ID [ "
                                + job.getJobID() 
                                + " ], archive ID [ "
                                + archive.getArchiveID()
                                + " ] does not contain a list of files to "
                                + "archive.");
                    }
            	}
            	builder.numArchivesComplete(numArchivesComplete);
                // The number of hashes complete is maintained for backwards 
                // compatibility.  It will always be the same as the number of 
                // archives complete
                builder.numHashesComplete(numArchivesComplete);
                builder.numFilesComplete(numFilesComplete);
                builder.sizeComplete(totalSizeComplete);
            }
            else {
            	LOGGER.error("The job ID requested [ "
            			+ job.getJobID()
            			+ " ] does not contain any individual archive jobs.  "
            			+ "This is invalid.");
            }
    	}
    	else {
    		LOGGER.warn("Input Job object is null but the database tier did "
    				+ "not raise a NoResultException.");
    	}
    }
    
    /**
     * Calculate the current statistics information associated with current 
     * in-progress job.
     * 
     * @param jobID The jobID requested by the client.
     * @return A populated JobTrackerMessage containing the current state of 
     * the job in progress.
     */
    public JobTrackerMessage getJobTracker(String jobID) {
        
    	JobTrackerMessageBuilder builder = 
    			new JobTrackerMessage.JobTrackerMessageBuilder();
        
        if ((jobID != null) && (!jobID.isEmpty())) {
        	builder.jobID(jobID);
            try {
            	getJobTrackerMessage(getJobService().getJob(jobID), builder); 
            }
            catch (NoResultException nre) {
            	LOGGER.warn("The database tier raised a NoResultsException "
            			+ "while looking up job ID [ "
            			+ jobID
            			+ " ].  Exception message => [ "
            			+ nre.getMessage()
            			+ " ].");
            }
            catch (ServiceUnavailableException sue) {
            	LOGGER.error("Internal system failure.  Target EJB service "
            			+ "is unavailable.  Exception message => [ "
            			+ sue.getMessage()
            			+ " ].");
            }
        }
        else {
            LOGGER.error("The input job ID is null or not populated.  Unable "
                    + "to determine job state.");
        }
        return builder.build();
    }

}
