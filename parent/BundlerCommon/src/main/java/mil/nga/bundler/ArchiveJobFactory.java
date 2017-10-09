package mil.nga.bundler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.model.Archive;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.bundler.model.ArchiveJob;
import mil.nga.bundler.model.ExtendedArchiveElement;
import mil.nga.bundler.model.ExtendedFileEntry;
import mil.nga.bundler.model.FileEntry;
import mil.nga.bundler.types.ArchiveType;

/**
 * 
 * @author L. Craig Carpenter
 *
 */
public class ArchiveJobFactory implements BundlerConstantsI {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    static final Logger LOGGER = LoggerFactory.getLogger(
            ArchiveJobFactory.class);
    
    /**
     * The target size of the output archive.
     */
    private long targetArchiveSize = DEFAULT_ARCHIVE_SIZE * BYTES_PER_MEGABYTE;
    
    /**
     * The target output archive type.
     */
    private ArchiveType archiveType = ArchiveType.ZIP;
    
    /**
     * The object to be used for generating output file names.
     */
    private FileNameGenerator fnGenerator;
    
    /**
     * Default no-arg constructor
     */
    public ArchiveJobFactory() { 
    	setArchiveType(ArchiveType.ZIP);
    	setTargetArchiveSize(DEFAULT_ARCHIVE_SIZE);
    	setFileNameGenerator();
    }
    
    /**
     * Alternate constructor allowing clients to set the type of output 
     * archive on construction.  This constructor will utilize default 
     * values for the target archive size and staging area.
     */
    public ArchiveJobFactory(ArchiveType type) {
    	setArchiveType(type);
    	setTargetArchiveSize(DEFAULT_ARCHIVE_SIZE);
    	setFileNameGenerator();
    }

    /**
     * Alternate constructor allowing clients to set both the archive 
     * type and target archive size at construction time. 
     * 
     * @param type The output archive type.
     * @param targetArchiveSize The target archive size.  This cannot be 
     * smaller than <code>MIN_ARCHIVE_SIZE</code> or larger than 
     * <code>MAX_ARCHIVE_SIZE</code>.  If it is not supplied it will be 
     * set to <code>DEFAULT_ARCHIVE_SIZE</code>
     */
    public ArchiveJobFactory(ArchiveType type, long targetArchiveSize) {
    	setArchiveType(type);
    	setTargetArchiveSize(targetArchiveSize);
    	setTargetArchiveSize(DEFAULT_ARCHIVE_SIZE);
    	setFileNameGenerator();
    }
    
    /**
     * Alternate constructor allowing clients to set the archive 
     * type, target archive size, and jobID at construction 
     * time. 
     * 
     * @param type The output archive type.
     * @param targetArchiveSize The target archive size.  This cannot be 
     * smaller than <code>MIN_ARCHIVE_SIZE</code> or larger than 
     * <code>MAX_ARCHIVE_SIZE</code>.  If it is not supplied it will be 
     * set to <code>DEFAULT_ARCHIVE_SIZE</code>
     * @param jobID Output location for the archives to be constructed.
     */
    public ArchiveJobFactory(
    		ArchiveType type, 
    		long targetArchiveSize, 
    		String jobID) {
    	setArchiveType(type);
    	setTargetArchiveSize(targetArchiveSize);
    	setFileNameGenerator(jobID);
    }
    
    /**
     * Alternate constructor allowing clients to set the archive 
     * type, target archive size, and output staging area at construction 
     * time. 
     * 
     * @param type The output archive type.
     * @param targetArchiveSize The target archive size.  This cannot be 
     * smaller than <code>MIN_ARCHIVE_SIZE</code> or larger than 
     * <code>MAX_ARCHIVE_SIZE</code>.  If it is not supplied it will be 
     * set to <code>DEFAULT_ARCHIVE_SIZE</code>
     * @param jobID The jobID to utilize.
     * @param fileName The output filename template. 
     */
    public ArchiveJobFactory(
    		ArchiveType type, 
    		long targetArchiveSize, 
    		String jobID,
    		String fileName) {
    	setArchiveType(type);
    	setTargetArchiveSize(targetArchiveSize);
    	setFileNameGenerator(jobID, fileName);
    }
    
    /**
     * Alternate constructor allowing clients to set all possible values
     * at construction time.
     * 
     * @param type The output archive type.
     * @param targetArchiveSize The target archive size.  This cannot be 
     * smaller than <code>MIN_ARCHIVE_SIZE</code> or larger than 
     * <code>MAX_ARCHIVE_SIZE</code>.  If it is not supplied it will be 
     * set to <code>DEFAULT_ARCHIVE_SIZE</code>
     * @param jobID The jobID to utilize.  This parameter is essentially 
     * ignored.  It was only retained to allow us to avoid duplicate 
     * method signatures.
     * @param fileName The output filename template.
     * @param stagingArea The output directory in which to store the output 
     * archive files. 
     */
    public ArchiveJobFactory(
    		ArchiveType type, 
    		long targetArchiveSize, 
    		String jobID,
    		String fileName,
    		String stagingArea) {
    	setArchiveType(type);
    	setTargetArchiveSize(targetArchiveSize);
    	setFileNameGenerator("", fileName, stagingArea);
    }
    
    /**
     * Main method used to break up a list of files into individual output 
     * archives that will ultimately be passed through the bundler for 
     * compression.
     * 
     * @param fileList The list of files that need to be broken up into 
     * individual archives.
     * @return The list of individual archives to be constructed.
     */
    public List<Archive> createArchivesFromFileEntry(List<FileEntry> fileList) {
    	
    	List<Archive> archives = new ArrayList<Archive>();
    	long             startTime = System.currentTimeMillis();
    	
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug("Generating archive jobs.");
    		LOGGER.debug(this.toString());
    	}
    	
    	if ((fileList != null) && (!fileList.isEmpty())) {
    		
    		// Get the estimated compressed size of each file.
            List<ExtendedFileEntry> decorated = CompressionEstimator
                    .getInstance()
                    .getEstimatedCompressedSizeForFileEntry(
                    		fileList, getArchiveType());
    		
            if ((decorated != null) && (decorated.size() > 0)) {
            	
            	int counter = 0;
	            Archive.ArchiveBuilder builder = new Archive.ArchiveBuilder();
	            
	            for (ExtendedFileEntry element : decorated) {
	            	
	            	// Will the file fit in the current archive?
	                if ((builder.getSize() + element.getEstimatedCompressedSize()) <
	                		getTargetArchiveSize()) {
	                	
	                    builder.element(
	                            element.getArchiveElement(), 
	                            element.getEstimatedCompressedSize());
	                }
	                else {
	                	
	                    builder.type(getArchiveType());
	                    builder.id(counter);
	                    builder.outputFileName(
	                    		getFileNameGenerator().getOutputFile(counter));
	                    archives.add(builder.build());
	                    builder = new Archive.ArchiveBuilder();
	                    builder.element(
	                            element.getArchiveElement(), 
	                            element.getEstimatedCompressedSize());
	                    counter++;
	                }
	            }
            }
            else {
            	LOGGER.error("Unable to estimate the size of the target "
            			+ "files.  The returned archive list will be empty.");
            }
            
    	}
        else {
            LOGGER.warn("The input list of FileEntry objects is null or "
                    + "empty.  The output list will be empty.");
        }
    	
        LOGGER.info("Archive job creation completed in [ "
                + (System.currentTimeMillis() - startTime)
                + " ].");
    	
    	return archives;
    }
    
    /**
     * Main method used to break up a list of files into individual output 
     * archives that will ultimately be passed through the bundler for 
     * compression.
     * 
     * @param fileList The list of files that need to be broken up into 
     * individual archives.
     * @return The list of individual archives to be constructed.
     */
    public List<Archive> createArchives(List<ArchiveElement> fileList) {
    	
    	List<Archive> archives = new ArrayList<Archive>();
    	long         startTime = System.currentTimeMillis();
    	
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug("Generating archive jobs.");
    		LOGGER.debug(this.toString());
    	}
    	
    	if ((fileList != null) && (!fileList.isEmpty())) {
            
    		// Get the estimated compressed size of each file.
            List<ExtendedArchiveElement> decorated = CompressionEstimator
                    .getInstance()
                    .getEstimatedCompressedSize(fileList, getArchiveType());
    		
            if ((decorated != null) && (decorated.size() > 0)) {
            	
            	int counter = 0;
	            Archive.ArchiveBuilder builder = new Archive.ArchiveBuilder();
	            
	            for (ExtendedArchiveElement element : decorated) {
	            	
	            	// Will the file fit in the current archive?
	                if ((builder.getSize() + element.getEstimatedCompressedSize()) <
	                		getTargetArchiveSize()) {
	                	
	                    builder.element(
	                            element.getArchiveElement(), 
	                            element.getEstimatedCompressedSize());
	                }
	                else {
	                	
	                    builder.type(getArchiveType());
	                    builder.id(counter);
	                    builder.outputFileName(getFileNameGenerator().getOutputFile(counter));
	                    archives.add(builder.build());
	                    builder = new Archive.ArchiveBuilder();
	                    builder.element(
	                            element.getArchiveElement(), 
	                            element.getEstimatedCompressedSize());
	                    counter++;
	                }
	            }
            }
            else {
            	LOGGER.error("Unable to estimate the size of the target "
            			+ "files.  The returned archive list will be empty.");
            }
            
    	}
        else {
            LOGGER.warn("The input list of ArchiveElement objects is null or "
                    + "empty.  The output list will be empty.");
        }
    	
        LOGGER.info("Archive job creation completed in [ "
                + (System.currentTimeMillis() - startTime)
                + " ].");
    	
    	return archives;
    }
    
    /**
     * Setter method for the type of output archive to create.
     * @param type The output archive type.
     */
    public ArchiveType getArchiveType() {
    	return archiveType;
    }
    
    /**
     * Getter method for the <code>FileNameGenerator</code> object that will 
     * be used in constructing the output file names. 
     * 
     * @param jobID The job ID to use in archive creation (may be empty).
     * @param filename Filename template to use for output archives.
     */
    private FileNameGenerator getFileNameGenerator() {
    	return fnGenerator;
    }
    
    /**
     * Getter method for the target archive size.
     * 
     * @return The target archive size.
     */
    public long getTargetArchiveSize() {
    	return targetArchiveSize;
    }
    
    /**
     * Setter method for the type of output archive to create.
     * @param type The output archive type.
     */
    private void setArchiveType(ArchiveType type) {
    	archiveType = type;
    }
    
    /**
     * Create the <code>FileNameGenerator</code> object that will be used in
     * constructing the output file names.  This method creates a 
     * <code>FileNameGenerator</code> object using all defaults.  
     */
    private void setFileNameGenerator() {
    	fnGenerator = new FileNameGenerator(getArchiveType());
    }

    /**
     * Create the <code>FileNameGenerator</code> object that will be used in
     * constructing the output file names.  This method creates a 
     * <code>FileNameGenerator</code> object using a custom staging area.
     */
    private void setFileNameGenerator(String jobID) {
    	fnGenerator = new FileNameGenerator(getArchiveType(), jobID);
    }
    
    /**
     * Create the <code>FileNameGenerator</code> object that will be used in
     * constructing the output file names.  This method creates a 
     * <code>FileNameGenerator</code> object using a custom staging area.
     */
    private void setFileNameGenerator(String jobID, String fileName) {
    	fnGenerator = new FileNameGenerator(getArchiveType(), jobID, fileName);
    }
    
    /**
     * Create the <code>FileNameGenerator</code> object that will be used in
     * constructing the output file names. 
     * 
     * @param jobID The job ID to use in archive creation (may be empty).
     * @param filename Filename template to use for output archives.
     */
    private void setFileNameGenerator(String jobID, String filename, String stagingArea) {
    	fnGenerator = new FileNameGenerator(getArchiveType(), jobID, filename);
    	fnGenerator.setStagingArea(stagingArea);
    }
    
    /**
     * Setter method for the target archive size to create.
     * 
     * @param value The target archive size in MB.  This cannot be 
     * smaller than <code>MIN_ARCHIVE_SIZE</code> or larger than 
     * <code>MAX_ARCHIVE_SIZE</code>.  If it is not supplied it will be 
     * set to <code>DEFAULT_ARCHIVE_SIZE</code>
     */
    public void setTargetArchiveSize(long value) {
    	if (value < MIN_ARCHIVE_SIZE) {
    		targetArchiveSize = MIN_ARCHIVE_SIZE * BYTES_PER_MEGABYTE;
    	}
    	else if (value > MAX_ARCHIVE_SIZE) {
    		targetArchiveSize = MAX_ARCHIVE_SIZE * BYTES_PER_MEGABYTE;
    	}
    	else {
    		targetArchiveSize = value * BYTES_PER_MEGABYTE;
    	}
    }
    
    /**
     * Print out the parameters that will be used for debugging purposes.
     */
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("ArchiveJobFactory Parameters : ");
    	sb.append("Archive Type => [ ");
    	sb.append(getArchiveType().getText());
    	sb.append(" ], Target archive size => [ ");
    	sb.append(getTargetArchiveSize());
    	sb.append(" ].");
    	sb.append(System.getProperty("line.separator"));
    	sb.append(getFileNameGenerator().toString());
    	return sb.toString();
    }
}
