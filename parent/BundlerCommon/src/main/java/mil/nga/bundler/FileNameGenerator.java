package mil.nga.bundler;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.PropertyLoader;
import mil.nga.bundler.exceptions.PropertiesNotLoadedException;
import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.types.ArchiveType;
import mil.nga.util.JobIDGenerator;
import mil.nga.util.URIUtils;

/**
 * Class used in the generation of output file names/URIs for individual 
 * bundler jobs.
 * 
 * @author L. Craig Carpenter
 */
public class FileNameGenerator 
        extends PropertyLoader 
        implements BundlerConstantsI {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    static final Logger LOGGER = LoggerFactory.getLogger(
            FileNameGenerator.class);
    
    /**
     * The job ID that will be used in the generation of output file names.
     */
    private String jobID;
    
    /**
     * Staging area used for the output archives.
     */
    private String stagingArea;
    
    /**
     * The template to use for the output filename.
     */
    private String filenameTemplate;
    
    /**
     * Archive type that will be created.
     */
    private ArchiveType type;
    
    /**
     * The file path separator
     */
    private String pathSeparator = null;
    
    /**
     * Default constructor that sets the value for the staging area.  If 
     * the staging area is not read from the Properties file, the JVM 
     * temporary directory is used.
     */
    private FileNameGenerator() {
        super(PROPERTY_FILE_NAME);
        
        String stagingArea = null;
        pathSeparator = System.getProperty("file.separator");
        
        try {
            stagingArea = getProperty(STAGING_DIRECTORY_PROPERTY);
        }
        catch (PropertiesNotLoadedException pnle) {
            LOGGER.warn("An unexpected PropertiesNotLoadedException " 
                    + "was encountered.  Please ensure the application "
                    + "is properly configured.  Using value of [ "
                    + "java.io.tmpdir"
                    + " ] for staging area.  Exception message => [ "
                    + pnle.getMessage()
                    + " ].");
        }
        setStagingArea(stagingArea);
    }
    
    /**
     * Alternate constructor allowing clients to specify the ArchiveType on 
     * construction.  The other required parameters (job ID and file name) 
     * will be default values.
     * 
     * @param type The archive type.
     */
    public FileNameGenerator(ArchiveType type) {
        this();
        setArchiveType(type);
        setJobID(null);
        setTemplateName(null);
    }
    
    /**
     * Alternate constructor allowing clients to specify the ArchiveType and 
     * job ID on construction.  The other required parameters (file name) 
     * will be default values.
     * 
     * @param type The archive type.
     * @param jobID The job ID to utilize.
     */
    public FileNameGenerator(ArchiveType type, String jobID) {
        this();
        setArchiveType(type);
        setJobID(jobID);
        setTemplateName(null);
    }
    
    /**
     * Alternate constructor allowing clients to specify the ArchiveType,  
     * job ID, and file name on construction.  
     * 
     * @param type The archive type.
     * @param jobID The job ID to utilize.
     * @param fileName The template to use when generating the output file name.
     */
    public FileNameGenerator(ArchiveType type, String jobID, String fileName) {
        this();
        setArchiveType(type);
        setJobID(jobID);
        setTemplateName(fileName);
    }
    
    /**
     * Public method used to return the name of the target output directory.
     * @return The output directory path.
     */
    public URI getOutputDirectory() {
        StringBuilder sb = new StringBuilder();
        sb.append(stagingArea);
        if (!sb.toString().endsWith(pathSeparator)) {
            sb.append(pathSeparator);
        }
        sb.append(jobID);
        return getURI(sb.toString());
    }
    
    /**
     * Public method used to generate the URI of the target output file.
     * 
     * @param ID The archive ID.
     * @return The URI of the output filename.
     */
    public URI getOutputFile(int ID) {
        StringBuilder sb = new StringBuilder();
        sb.append(stagingArea);
        if (!sb.toString().endsWith(pathSeparator)) {
            sb.append(pathSeparator);
        }
        if (!jobID.isEmpty()) {
	        sb.append(jobID);
	        if (!sb.toString().endsWith(pathSeparator)) {
	            sb.append(pathSeparator);
	        }
        }
        sb.append(filenameTemplate);
        if (ID > 0) {
            sb.append("_");
            sb.append(ID);
        }
        sb.append(".");
        sb.append(type.getText().toLowerCase());
        return getURI(sb.toString());
    }
    
    /**
     * Get the default name of the archive file to use.
     * 
     * @return The archive filename.
     */
    public static String getFileName() {
        StringBuilder sb = new StringBuilder();
        sb.append(DEFAULT_FILENAME_PREFIX);
        sb.append("_data_archive");
        return sb.toString();
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
    private URI getURI(String filePath) 
            throws FileSystemNotFoundException {
        URI uri = null;
        if ((filePath != null) && (!filePath.isEmpty())) {
            uri = URIUtils.getInstance().getURI(filePath);
        }
        else {
        	LOGGER.warn("Input file path is null.  Unable to create URI.");
        }
        return uri;
    }
    
    /**
     * Generate the staging directory from the JVM properties information.
     * @return A staging directory on the local server.
     */
    private String genStagingAreaFromJVM () {
        StringBuilder sb = new StringBuilder();
        sb.append("file://");
        sb.append(System.getProperty("java.io.tmpdir"));
        if (!sb.toString().endsWith(pathSeparator)) {
            sb.append(pathSeparator);
        }
        return sb.toString();
    }
    
    /**
     * Setter method for the archive type.
     * @param type The output archive type.
     */
    private void setArchiveType(ArchiveType type) {
        if (type == null) {
            type = ArchiveType.ZIP;
        }
        else {
            this.type = type;
        }
    }
    
    /**
     * Setter method for the template name used for output archive files.
     * @param value The user-supplied template filename.
     */
    private void setTemplateName(String value) {
        if ((value == null) || (value.isEmpty())) {
            filenameTemplate = getFileName();
        }
        else {
            // Take whatever the user requested and strip off the extension.
            if (value.contains(".")) {
                filenameTemplate = value.substring(0, value.lastIndexOf('.'));
            }
            else {
                filenameTemplate = value;
            }
        }
    }
    
    /**
     * Setter method for the job ID field.  Modified to allow for a "empty" 
     * value for the job ID.  If the job ID is empty the output files will 
     * be written directly to the staging area. 
     * @param value The job ID.
     */
    private void setJobID(String value) {
        if (value == null) {
            jobID = JobIDGenerator.generateUniqueToken(2*UNIQUE_TOKEN_LENGTH);
        }
        else {
            jobID = value;
        }
    }
    
    /**
     * Public method used to set the target staging area.  This was modified 
     * to allow public access in the event offline tools want to specify an 
     * output location other than default staging area.
     * 
     * @param value The value of the staging area retrieved from the 
     * properties file.
     */
    public void setStagingArea(String value) {
        StringBuilder sb = new StringBuilder();
        if ((value == null) || (value.isEmpty())) {
            sb.append(genStagingAreaFromJVM());
        }
        else {
            sb.append(value);
            if (!sb.toString().endsWith(pathSeparator)) {
                sb.append(pathSeparator);
            }
        }
        stagingArea = sb.toString();
    }

    /**
     * Print out the parameters that will be utilized in constructing 
     * the output file names.
     */
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("FileNameGenerator Parameters : ");
    	sb.append("Archive Type => [ ");
    	sb.append(type.getText());
    	sb.append(" ], Job ID => [ ");
    	sb.append(jobID);
    	sb.append(" ], filename template => [ ");
    	sb.append(filenameTemplate);
    	sb.append(" ], staging area => [ ");
    	sb.append(stagingArea);
    	sb.append(" ].");
    	return sb.toString();
    }
    
    public static void main (String[] args) {
        
        FileNameGenerator generator = new FileNameGenerator(ArchiveType.BZIP2);
        System.out.println(generator.getOutputDirectory());
        System.out.println(generator.getOutputFile(0));
        System.out.println(generator.getOutputFile(1));
        System.out.println(generator.getOutputFile(2));
        
        generator = new FileNameGenerator(ArchiveType.ZIP, "", "test_output_archive");
        generator.setStagingArea("/mnt/public/data_bundles/test");
        System.out.println(generator.getOutputDirectory());
        System.out.println(generator.getOutputFile(0));
        System.out.println(generator.getOutputFile(1));
        System.out.println(generator.getOutputFile(2));
        
        generator = new FileNameGenerator(ArchiveType.TAR, "ABCDEFGHIJKLMNOPQRSTUV");
        System.out.println(generator.getOutputDirectory());
        System.out.println(generator.getOutputFile(0));
        System.out.println(generator.getOutputFile(1));
        System.out.println(generator.getOutputFile(2));
        
        generator = new FileNameGenerator(ArchiveType.ZIP, "ABCDEFGHIJKLMNOPQRSTUV", "test_output_archive");
        System.out.println(generator.getOutputDirectory());
        System.out.println(generator.getOutputFile(0));
        System.out.println(generator.getOutputFile(1));
        System.out.println(generator.getOutputFile(2));
    }
}
