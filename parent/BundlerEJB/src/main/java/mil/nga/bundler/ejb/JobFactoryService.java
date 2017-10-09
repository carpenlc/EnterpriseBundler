package mil.nga.bundler.ejb;

import java.util.List;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import mil.nga.PropertyLoader;
import mil.nga.bundler.ArchiveJobFactory;
import mil.nga.bundler.BundleRequest;
import mil.nga.bundler.FileValidator;
import mil.nga.bundler.UrlGenerator;
import mil.nga.bundler.exceptions.InvalidRequestException;
import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.messages.BundleRequestMessage;
import mil.nga.bundler.model.Archive;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.bundler.model.ArchiveJob;
import mil.nga.bundler.model.FileEntry;
import mil.nga.bundler.model.Job;
import mil.nga.bundler.types.ArchiveType;
import mil.nga.bundler.types.JobStateType;
import mil.nga.util.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session Bean implementation class JobFactoryService
 * 
 * Public methods were modified to be asynchronous.  This was done to more
 * robustly handle the very large bundle requests that we started receiving 
 * from the MPSU team.
 */
@Stateless
@LocalBean
public class JobFactoryService 
        extends PropertyLoader implements BundlerConstantsI {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
            JobFactoryService.class);
    
    /**
     * Container-injected reference to the JobService EJB.
     */
    @EJB
    JobService jobService;
    
    /**
     * Container-injected reference to the JobRunnerService EJB.
     */
    @EJB
    JobRunnerService jobRunner;
    
    /**
     * Default constructor.
     */
    public JobFactoryService() { 
        super(PROPERTY_FILE_NAME);
    }
    
    /**
     * Private method used to obtain a reference to the target EJB.  
     * 
     * @return Reference to the JobService EJB.
     */
    private JobRunnerService getJobRunnerService() {
        if (jobRunner == null) {
            LOGGER.warn("Application container failed to inject the "
                    + "reference to JobRunnerService.  Attempting to "
                    + "look it up via JNDI.");
            jobRunner = EJBClientUtilities
                    .getInstance()
                    .getJobRunnerService();
        }
        return jobRunner;
    }
    
    /**
     * Private method used to obtain a reference to the target EJB.  
     * 
     * @return Reference to the JobService EJB.
     */
    private JobService getJobService() {
        if (jobService == null) {
            LOGGER.warn("Application container failed to inject the "
                    + "reference to JobService.  Attempting to "
                    + "look it up via JNDI.");
            jobService = EJBClientUtilities
                    .getInstance()
                    .getJobService();
        }
        return jobService;
    }
    
    /**
     * Generate the name of the accompanying hash file from the calculated
     * name of the output archive.
     * 
     * @param archiveFile The name of the output archive.
     * @return The name of the output hash file.
     */
    private String getHashFile(String archiveFile) {
    	String temp = null;
    	if ((archiveFile != null) && (!archiveFile.isEmpty())) {
	    	temp = FileUtils.removeExtension(archiveFile);
	    	temp = temp + "." + HASH_FILE_EXTENSION;
    	}
    	return temp;
    }
    
    /**
     * Simple method to calculate the target output archive size in
     * bytes.
     * 
     * @param sizeMB Target size measured in MB.
     * @return The target size in bytes.
     */
    private long getSizeInBytes(long sizeMB) {
    	return sizeMB * BYTES_PER_MEGABYTE;
    }
    
    /**
     * Create a concrete instance of a <code>ArchiveJob</code> object that 
     * will be added to the target job.
     * 
     * @param jobID The job ID.
     * @param archive The <code>Archive</code> object to be converted to an
     * <code>ArchiveJob</code> object.
     * @return
     */
    private ArchiveJob createArchiveJobInstance(
    		String  jobID,
    		Archive archive) {
    	
    	int        numFiles   = 0;
    	long       size       = 0;
    	ArchiveJob archiveJob = null;
    	
    	if ((archive.getElementList() != null) && 
    			(archive.getElementList().size() > 0)) {
    		
    		archiveJob = new ArchiveJob(
        			jobID, archive.getID(), archive.getType());
	    	archiveJob.setArchive(
	    			archive.getOutputFile().toString());
	    	archiveJob.setArchiveURL(
	    			UrlGenerator.getInstance().toURL(
	    					archiveJob.getArchive()));
	    	archiveJob.setHash(getHashFile(archiveJob.getArchive()));
	    	archiveJob.setHashURL(
	    			UrlGenerator.getInstance().toURL(
	    					archiveJob.getHash()));
	    	archiveJob.setArchiveState(JobStateType.NOT_STARTED);
	    	
    		for (ArchiveElement element : archive.getElementList()) {
    			numFiles++;
    			size += element.getSize();
	    		archiveJob.add(
	    				createFileEntryInstance(
	    						jobID, 
	    						archive.getID(), 
	    						element));
	    	}
    		
    		archiveJob.setNumFiles(numFiles);
    		archiveJob.setSize(size);
    		
    	}
    	else {
    		LOGGER.error("The input Archive object contains no associated files "
    				+ "to archive.  A null ArchiveJob will be returned.");
    	}
    	return archiveJob;
    }
    
    /**
     * Create a concrete instance of a <code>FileEntry</code> object that 
     * we can add to the target Job.
     * 
     * @param jobID The job ID.
     * @param archiveID The archive ID.
     * @param element The ArchiveElement object.
     * @return A constructed and populated <code>FileEntry</code> object.
     */
    private FileEntry createFileEntryInstance(
    		String jobID, 
    		long archiveID,
    		ArchiveElement element) {
    	return new FileEntry(
    			jobID, 
    			archiveID, 
    			element.getURI().toString(), 
    			element.getEntryPath(), 
    			element.getSize());
    }
    
    /**
     * Method used to construct a job object that will be used to notify the 
     * caller of an invalid request.
     * 
     * @param jobID The job ID.
     * @param userName The client user submitting the job.
     * @param type The type of output archive to create.
     * @param archiveSize The target size of the output archive.
     * @return A failed Job.
     */
    private Job createBogusJobInstance(
    		String        jobID, 
    		String        userName, 
    		ArchiveType   type,
    		long          archiveSize) {
    	Job job = new Job();
    	job.setJobID(jobID);
    	job.setUserName(userName);
    	job.setArchiveType(type);
    	job.setArchiveSize(getSizeInBytes(archiveSize));
    	job.setState(JobStateType.INVALID_REQUEST);
    	return job;
    }
    
    /**
     * Create a concrete instance of a <code>Job</code> object that will 
     * be used to 
     * 
     * @param jobID The job ID.
     * @param userName The client user submitting the job.
     * @param type The type of output archive to create.
     * @param archiveSize The target size of the output archive.
     * @return A constructed and populated Job object.
     */
    private Job createJobInstance(
    		String        jobID, 
    		String        userName, 
    		ArchiveType   type,
    		long          archiveSize,
    		List<Archive> archives) {
    	
    	int  numFiles    = 0;
    	int  numArchives = 0;
    	long size        = 0;
    	Job  job         = new Job();
    	
    	job.setJobID(jobID);
    	job.setUserName(userName);
    	job.setArchiveType(type);
    	job.setArchiveSize(getSizeInBytes(archiveSize));
    	
    	if ((archives != null) && (archives.size() > 0)) {
    		for (Archive archive : archives) {
    			ArchiveJob aJob = createArchiveJobInstance(jobID, archive);
    			if (aJob != null) {
    				numArchives++;
    				numFiles += aJob.getNumFiles();
    				size     += aJob.getSize();
    				job.addArchive(aJob);
    			}
    			else {
    				LOGGER.warn("Null ArchiveJob object received.  "
    						+ "The ArchiveJob will not be added to the "
    						+ "target job.");
    			}
        	}
    	}
    	else {
    		LOGGER.error("There are no archives in the target job.  Setting "
    				+ "job state to INVALID_REQUEST.");
    		job.setState(JobStateType.INVALID_REQUEST);
    	}
    	
    	job.setTotalSize(size);
    	job.setNumFiles(numFiles);
    	job.setNumArchives(numArchives);
    	
    	return job;
    }

    @Asynchronous
    public void createJob(String jobID, BundleRequestMessage request) {
    	
    	long startTime = System.currentTimeMillis();
    	Job  job       = null;
    	
    	try {
    		
	    	// Validate and expand the input file list.
	        List<FileEntry> files = FileValidator
	                .getInstance()
	                .validate(request.getFiles());
	    	
	        if ((files != null) && (!files.isEmpty())) {
	        	
		        ArchiveJobFactory factory = new ArchiveJobFactory(
		        		request.getType(),
		        		request.getMaxSize(),
		        		jobID,
		        		request.getOutputFilename());
		        
		        List<Archive> archives = factory
		        		.createArchivesFromFileEntry(files);
		        
		        if ((archives != null) && (archives.size() > 0)) {
			        job = createJobInstance(
			        		jobID, 
			        		request.getUserName(), 
			        		request.getType(), 
			        		request.getMaxSize(),
			        		archives);
		        }
		        else {
		        	LOGGER.error("There are no archive jobs to process.  "
		        			+ "Setting job state to INVALID_REQUEST.");	
		        	job = createBogusJobInstance(
			        		jobID, 
			        		request.getUserName(), 
			        		request.getType(), 
			        		request.getMaxSize());
		        }
	        }
	        else {
	        	LOGGER.error("Validation algorithm revealed no files to "
	        			+ "bundle.  Setting job state to INVALID_REQUEST.");
	        	job = createBogusJobInstance(
		        		jobID, 
		        		request.getUserName(), 
		        		request.getType(), 
		        		request.getMaxSize());
	        }
        
    	}
    	catch (InvalidRequestException ire) {
    		LOGGER.error("InvalidRequestException raised while validating "
    				+ "the input job.  Exception message => [ "
    				+ ire.getMessage()
    				+ " ].");
    		job = createBogusJobInstance(
	        		jobID, 
	        		request.getUserName(), 
	        		request.getType(), 
	        		request.getMaxSize());
    	}
    	
    	// Save the job to the target data store.
    	if (getJobService() != null) {
    		getJobService().persist(job);
    	}
    	else {
    		LOGGER.error("Unable to look up the target JobService EJB.  "
    				+ "Unable to persist job.  Unable to process incoming "
    				+ "request.");
    	}
    	
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug("Job ID [ "
    				+ jobID 
    				+ " created in [ "
    				+ (System.currentTimeMillis() - startTime)
    				+ " ].");
    	}
    	
        runJob(job);
    }
    
    @Asynchronous
    public void createJob(String jobID, BundleRequest request) {
    	
    	long startTime = System.currentTimeMillis();
    	Job  job       = null;
    	
    	try {
    		
	    	// Validate and expand the input file list.
	        List<FileEntry> files = FileValidator
	                .getInstance()
	                .validateStringList(request.getFiles());
	        
	        if ((files != null) && (!files.isEmpty())) {
	        
		        ArchiveJobFactory factory = new ArchiveJobFactory(
		        		request.getType(),
		        		request.getMaxSize(),
		        		jobID,
		        		request.getOutputFilename());
		        
		        List<Archive> archives = factory
		        		.createArchivesFromFileEntry(files);
		        
		        if ((archives != null) && (archives.size() > 0)) {
			        job = createJobInstance(
			        		jobID, 
			        		request.getUserName(), 
			        		request.getType(), 
			        		request.getMaxSize(),
			        		archives);
		        }
		        else {
		        	LOGGER.error("There are no archive jobs to process.  "
		        			+ "Setting job state to INVALID_REQUEST.");	
		        	job = createBogusJobInstance(
			        		jobID, 
			        		request.getUserName(), 
			        		request.getType(), 
			        		request.getMaxSize());
		        }
	        }
	        else {
	        	LOGGER.error("Validation algorithm revealed no files to "
	        			+ "bundle.  Setting job state to INVALID_REQUEST.");
	        	job = createBogusJobInstance(
		        		jobID, 
		        		request.getUserName(), 
		        		request.getType(), 
		        		request.getMaxSize());
	        }
	    }
		catch (InvalidRequestException ire) {
			LOGGER.error("InvalidRequestException raised while validating "
					+ "the input job.  Exception message => [ "
					+ ire.getMessage()
					+ " ].");
			job = createBogusJobInstance(
	        		jobID, 
	        		request.getUserName(), 
	        		request.getType(), 
	        		request.getMaxSize());
		}
    	
    	// Save the job to the target data store.
    	if (getJobService() != null) {
    		getJobService().persist(job);
    	}
    	else {
    		LOGGER.error("Unable to look up the target JobService EJB.  "
    				+ "Unable to persist job.  Unable to process incoming "
    				+ "request.");
    	}
    	
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug("Job ID [ "
    				+ jobID 
    				+ " created in [ "
    				+ (System.currentTimeMillis() - startTime)
    				+ " ].");
    	}
    	
    	runJob(job);
    }

    /**
     * Start execution of the target job.
     * 
     * @param job The job to execute.
     */
    public void runJob(Job job) {
    	if (job != null) {
    		if (job.getState() == JobStateType.NOT_STARTED) {
	    		if (getJobRunnerService() != null) {
	    			LOGGER.info("Starting job ID [ "
	    					+ job.getArchiveSize()
	    					+ " ].");
	    			getJobRunnerService().run(job);
	    		}
    		}
    		else {
    			LOGGER.warn("Attempted to start a job with a state of [ "
    					+ job.getState().getText()
    					+ " ].  Job will not be started.");
    		}
    	}
    	else {
    		LOGGER.warn("Input job is null.  Nothing to do.");
    	}
    }
}
