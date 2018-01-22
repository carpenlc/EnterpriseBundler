package mil.nga.bundler.messages;

import java.io.Serializable;
import java.lang.String;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.model.ArchiveJob;
import mil.nga.bundler.types.JobStateType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * JPA entity following the Java bean pattern that holds information on the
 * status of in-progress jobs.  This object is built from data extracted from 
 * <code>Job</code> objects.  Also includes JAX-B annotations for conversion 
 * to XML/JSON.
 * 
 * Had some problems deploying this application to JBoss.  Though the Jersey
 * annotations (Xml*) should have been sufficient, JBoss would not 
 * interpret the input as JSON.  We added the the Jackson annotations to work
 * around the issue.
 * 
 * Ran into even more problems deploying to Wildfly.  Had to upgrade to Jackson
 * 2.x annotations (i.e. com.fasterxml vs. org.codehaus).  We also had to remove
 * the Jersey (i.e. XML) annotations.  Further, we had to add the Jackson 
 * JsonIgnore annotation to all of the "getter" methods or else we ended up 
 * with two copies of the data in the output message.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = JobTrackerMessage.JobTrackerMessageBuilder.class)
public class JobTrackerMessage implements BundlerConstantsI, Serializable {

    /**
     * Eclipse-generated serialVersionUID
     */
    private static final long serialVersionUID = -5071138637154858714L;
    
    // Internal members.
    private final List<ArchiveJob> archives;
    private final long             elapsedTime;
    private final String           jobID;
    private final int              numArchives;
    private final int              numArchivesComplete;
    private final long             numFiles;
    private final long             numFilesComplete;
    private final int              numHashesComplete;
    private final JobStateType     state;
    private final long             totalSize;
    private final long             totalSizeComplete;
    private final String           userName;
    
    /**
     * Private constructor forcing the builder design pattern.  
     * @param The builder object. 
     */
    private JobTrackerMessage(JobTrackerMessageBuilder builder) {
    	this.numArchives         = builder.numArchives;
    	this.numArchivesComplete = builder.numArchivesComplete;
        this.elapsedTime         = builder.elapsedTime;
        this.numHashesComplete   = builder.numHashesComplete;
        this.numFiles            = builder.numFiles;
        this.numFilesComplete    = builder.numFilesComplete;
        this.totalSize           = builder.totalSize;
        this.totalSizeComplete   = builder.totalSizeComplete;
        this.archives            = builder.archives;
        this.jobID               = builder.jobID;
        this.state               = builder.state;
    	this.userName            = builder.userName;
    }
        
    /**
     * Getter method for the list of archives created by the archive job.
     * @return The list of bundles created.
     */
    @JsonProperty(value="archives")
    public Collection<ArchiveJob> getArchives() {
        return archives;
    }
    
    /**
     * Get the elapsed time for the archive job. 
     * @return The elapsed time (in milliseconds)
     */
    @JsonProperty(value="elapsed_time")
    public long getElapsedTime() {
        return elapsedTime;
    }
    
    /**
     * Getter method for the job ID associated with these metrics.
     * @return The job ID.
     */
    @JsonProperty(value="job_id")
    public String getJobID() {
        return jobID;
    }

    /**
     * The user who submitted the job
     * @return The user
     */
    @JsonProperty(value="user_name")
    public String getUserName() {
        return userName;
    }
    
    /**
     * Getter method for the number of archive files to be created by the 
     * job.
     * @return The number of archive files to create.
     */
    @JsonProperty(value="threads")
    public int getNumArchives() {
        return numArchives;
    }
    
    /**
     * Getter method for the number of archive files that have been completed.
     * @return The number of archive files to create.
     */
    @JsonProperty(value="threads_complete")
    public int getNumArchivesComplete() {
        return numArchivesComplete;
    }
    
    /**
     * Getter method for the number of hash files that have been created.
     * @return The number of hash files created.
     */
    @JsonProperty(value="hashes_complete")
    public int getNumHashesComplete() {
        return numHashesComplete;
    }
    
    /**
     * Getter method for the total number of files to be processed by the job.
     * @return The total number of files to be processed.
     */
    @JsonProperty(value="num_files")
    public long getNumFiles() {
        return numFiles;
    }
    
    /**
     * Getter method for the number of files processed.
     * @return The number of files processed.
     */
    @JsonProperty(value="files_complete")
    public long getNumFilesComplete() {
        return numFilesComplete;
    }
    
    /**
     * Getter method for the total uncompressed size of the job.
     * @return The total uncompressed size of the job.
     */
    @JsonProperty(value="size")
    public long getTotalSize() {
        return totalSize;
    }
    
    /**
     * Getter method for the total size that has been processed by the job
     * @return The amount of data that has been processed.
     */
    @JsonProperty(value="size_complete")
    public long getSizeComplete() {
        return totalSizeComplete;
    }
    
    /**
     * Getter method for the current state of the job in progress.
     * @return The current state of the job.
     */
    @JsonProperty(value="state")
    public JobStateType getState() {
        return state;
    }
    
    /**
     * Overridden toString method used to output relevant statistics data in
     * plain text.
     */
    @Override
    public String toString() {
        String        newLine = System.getProperty("line.separator");
        StringBuilder sb      = new StringBuilder();
        sb.append("----------------------------------------");
        sb.append("----------------------------------------");
        sb.append(newLine);
        sb.append("Job Tracker (ID: ");
        sb.append(getJobID());
        sb.append(")");
        sb.append(newLine);
        sb.append("----------------------------------------");
        sb.append("----------------------------------------");
        sb.append(newLine);
        sb.append("User                           : ");
        sb.append(getUserName());
        sb.append(newLine);
        sb.append("Job State                      : ");
        sb.append(getState());
        sb.append(newLine);
        sb.append("Number of Archives             : ");
        sb.append(getNumArchives());
        sb.append(newLine);
        sb.append("Number of Archives Complete    : ");
        sb.append(getNumArchivesComplete());
        sb.append(newLine);
        sb.append("Number of Hashes Complete      : ");
        sb.append(getNumHashesComplete());
        sb.append(newLine);
        sb.append("Total Number of Files          : ");
        sb.append(getNumFiles());
        sb.append(newLine);
        sb.append("Total Number of Files Complete : ");
        sb.append(getNumFilesComplete());
        sb.append(newLine);
        sb.append("Total Size                     : ");
        sb.append(getTotalSize());
        sb.append("  (Uncompressed)");
        sb.append(newLine);
        sb.append("Total Size Complete            : ");
        sb.append(getSizeComplete());
        sb.append("  (Compressed)");
        sb.append(newLine);
        sb.append("Elapsed Time                   : ");
        sb.append(getElapsedTime());
        sb.append(" ms ");
        sb.append(newLine);
        if ((archives != null) && (archives.size() > 0)) {
            sb.append("Output Archives                : ");
            sb.append(newLine);
            for (ArchiveJob bundle : archives) {
                sb.append(bundle.toString());
            }
        }
        sb.append(newLine);
        sb.append("----------------------------------------");
        sb.append("----------------------------------------");
        sb.append(newLine);
        return sb.toString();
    }
   
    /**
     * Internal static class implementing the Builder creation pattern for 
     * new JobTrackerMessageBuilder objects.  
     * 
     * @author L. Craig Carpenter
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class JobTrackerMessageBuilder implements BundlerConstantsI {
        
    	// Private internal members
    	private int             numArchives         = 0;
        private int             numArchivesComplete = 0;
        private long            elapsedTime         = 0L;
        private int             numHashesComplete   = 0;
        private long            numFiles            = 0;
        private long            numFilesComplete    = 0;
        private long            totalSize           = 0L;
        private long            totalSizeComplete   = 0L;
        private List<ArchiveJob> archives           = new ArrayList<ArchiveJob>();
        private String           jobID              = null;
        private JobStateType     state              = JobStateType.NOT_AVAILABLE;
        private String           userName           = DEFAULT_USERNAME;
        
        /**
         * Method called as archive jobs complete.  This saves a list of 
         * completed archive files.  In the new cloud deployment this method
         * is responsible for updating all of the internal pointers in order
         * to keep the the job tracker data in check.
         * 
         * @param value Metadata associated with a completed archive file.
         */
        @JsonIgnore
        public JobTrackerMessageBuilder archive(ArchiveJob value) {
            if (archives == null) {
                archives = new ArrayList<ArchiveJob>();
            }
            archives.add(value);
            return this;
        }
        
        /**
         * Setter method for the list of completed <code>ArchiveJob</code> 
         * objects.
         * 
         * @param completedArchives Metadata associated with a completed archive file.
         */
        @JsonProperty(value="archives")
        public JobTrackerMessageBuilder archives(List<ArchiveJob> value) {
            archives = value;
            return this;
        }
        
        /**
         * Method used to construct an object of type <code>JobTrackerMessage</code>.
         * 
         * @return Instantiated <code>JobTrackerMessage</code> object.
         * @throws IllegalStateException If any individual fields are invalid.
         */
        public JobTrackerMessage build() throws IllegalStateException {
        	JobTrackerMessage object = new JobTrackerMessage(this);
        	validateJobTrackerMessage(object);
        	return object;
        }
        
        /**
         * Setter method for the ID associated with the job.
         * @param ID The associated job ID
         */
        @JsonProperty(value="job_id")
        public JobTrackerMessageBuilder jobID(String value) {
            jobID = value;
            return this;
        }

        /**
         * Setter method for the number of archive files that will be created by 
         * the current job.
         * @param value The number of archive files to create.
         */
        @JsonProperty(value="threads")
        public JobTrackerMessageBuilder numArchives(int value) {
            numArchives = value;
            return this;
        }
        
        /**
         * Setter method for the total number of files this job is expected to 
         * process.
         * @param size The total number of files to be processed by the job.
         */
        @JsonProperty(value="num_files")
        public JobTrackerMessageBuilder numFiles(long value) {
            numFiles = value;
            return this;
        }
        
        /**
         * Setter method for the total number of files this job is expected to 
         * process.
         * @param size The total number of files to be processed by the job.
         */
        @JsonProperty(value="files_complete")
        public JobTrackerMessageBuilder numFilesComplete(long value) {
            numFilesComplete = value;
            return this;
        }
        
        /**
         * Setter method for the number of hash files that have been created.
         * @param value number of hash files created.
         */
        @JsonProperty(value="hashes_complete")
        public JobTrackerMessageBuilder numHashesComplete(int value) {
            numHashesComplete = value;
            return this;
        }
        
        /**
         * Setter method for the total amount of uncompressed data this job is 
         * expected to process.
         * @param size The total size of the job.
         */
        @JsonProperty(value="size")
        public JobTrackerMessageBuilder totalSize(long value) {
            totalSize = value;
            return this;
        }
        
        /**
         * Setter method for the amount of wall-clock time consumed by the job.
         * @param time elapsed time in milliseconds.
         */
        @JsonProperty(value="elapsed_time")
        public JobTrackerMessageBuilder elapsedTime(long value) {
            elapsedTime = value;
            return this;
        }
       
        /**
         * Setter method for the number of archive files that have completed 
         * processing.
         * @param value The number of archive files complete.
         */
        @JsonProperty(value="threads_complete")
        public JobTrackerMessageBuilder numArchivesComplete(int value) {
            numArchivesComplete = value;
            return this;
        }
        
        /**
         * Setter method for the current state of the job.
         * @param state The state of the job
         */
        @JsonProperty(value="state")
        public JobTrackerMessageBuilder state(JobStateType state) {
            this.state = state;
            return this;
        }
        
        /**
         * Setter method for the size of the data completed.
         * @param value The amount of data processed.
         */
        @JsonProperty(value="size_complete")
        public JobTrackerMessageBuilder sizeComplete(long value) {
            totalSizeComplete = value;
            return this;
        }
        
        /**
         * Setter method for the username who submitted the job
         * @param value the user name
         */
        @JsonProperty(value="user_name")
        public JobTrackerMessageBuilder userName(String value) {
            userName = value;
            return this;
        }
        
        /**
         * Validate internal member variables.  This is called prior to
         * the actual construction of the parent object.
         * 
         * @param object The <code>JobTrackerMessage</code> object to validate.
         * @throws IllegalStateException Thrown if any of the required fields 
         * are not populated.
         */
        public void validateJobTrackerMessage(JobTrackerMessage object) 
        		throws IllegalStateException {
        	
        	if ((object.getNumArchives() < 0) || 
        			(object.getNumArchives() > MAX_NUM_ARCHIVES)) {
        		throw new IllegalStateException("numArchives parameter out "
        				+ "of range.  Value supplied [ "
        				+ object.getNumArchives()
        				+ " ].");
        	}
            if ((object.getNumArchivesComplete() < 0) ||
                    (object.getNumArchivesComplete() > MAX_NUM_ARCHIVES)) {
        		throw new IllegalStateException("numArchivesComplete "
        				+ "parameter out of range.  Value supplied [ "
        				+ object.getNumArchivesComplete()
        				+ " ].");
            }
            if (object.getNumArchivesComplete() > object.getNumArchives()) {
        		throw new IllegalStateException("numArchivesComplete "
        				+ "parameter out of range.  numArchivesComplete > "
        				+ "numArchives.  Value supplied [ "
        				+ object.getNumArchivesComplete()
        				+ " ].");
            }
            if (object.getElapsedTime() < 0) {
        		throw new IllegalStateException("elapsedTime "
        				+ "parameter out of range.  Value supplied [ "
        				+ object.getElapsedTime()
        				+ " ].");
            }
            if ((object.getNumHashesComplete() < 0) ||
                    (object.getNumHashesComplete() > MAX_NUM_ARCHIVES)) {
        		throw new IllegalStateException("numHashesComplete "
        				+ "parameter out of range.  Value supplied [ "
        				+ object.getNumHashesComplete()
        				+ " ].");
            }
            if (object.getNumHashesComplete() > object.getNumArchives()) {
        		throw new IllegalStateException("numHashesComplete "
        				+ "parameter out of range.  numHashesComplete > "
        				+ "numArchives.  Value supplied [ "
        				+ object.getNumHashesComplete()
        				+ " ].");
            }

            if (object.getNumFiles() < 0) { 
        		throw new IllegalStateException("numFiles "
        				+ "parameter out of range.  Value supplied [ "
        				+ object.getNumFiles()
        				+ " ].");
            }
            if (object.getNumFilesComplete() < 0) {
        		throw new IllegalStateException("numFilesComplete "
        				+ "parameter out of range.  Value supplied [ "
        				+ object.getNumFilesComplete()
        				+ " ].");
            }
            if (object.getNumFilesComplete() > object.getNumFiles()) {
        		throw new IllegalStateException("numFilesComplete "
        				+ "parameter out of range.  numFilesComplete > "
        				+ "numFiles.  Value supplied [ "
        				+ object.getNumFilesComplete()
        				+ " ].");
            }
            if (object.getTotalSize() < 0) { 
        		throw new IllegalStateException("totalSize "
        				+ "parameter out of range.  Value supplied [ "
        				+ object.getTotalSize()
        				+ " ].");
            }
            if (object.getSizeComplete() < 0) {
        		throw new IllegalStateException("sizeComplete "
        				+ "parameter out of range.  Value supplied [ "
        				+ object.getSizeComplete()
        				+ " ].");
            }
            if (object.getSizeComplete() > object.getTotalSize()) {
        		throw new IllegalStateException("sizeComplete "
        				+ "parameter out of range.  sizeComplete > "
        				+ "totalSize.  Value supplied [ "
        				+ object.getSizeComplete()
        				+ " ].");
            }
            if (object.getState() == null) {
           		throw new IllegalStateException("Job state not populated!");
            }
            if ((object.getJobID() == null) || (object.getJobID().isEmpty())) {
            	throw new IllegalStateException("Job ID is not populated!");
            }
            if ((object.getUserName() == null) || (object.getUserName().isEmpty())) {
            	throw new IllegalStateException("User name is not populated!");
            }
        }
    }
}
