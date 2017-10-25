package mil.nga.bundler.ejb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Properties;

import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mil.nga.PropertyLoader;
import mil.nga.bundler.BundleRequest;
import mil.nga.bundler.exceptions.PropertiesNotLoadedException;
import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.messages.BundleRequestMessage;
import mil.nga.bundler.messages.BundlerMessageSerializer;
import mil.nga.util.URIUtils;

/**
 * Session Bean implementation class RequestArchiveService
 * 
 * This class is mainly for debugging purposes.  As input it takes a 
 * BundlerRequest object and the job ID assigned to that request and
 * marshals the data in JSON format to an on disk file.  
 * 
 * @author L. Craig Carpenter
 */
@Stateless
@LocalBean
public class RequestArchiveService 
        extends PropertyLoader 
        implements BundlerConstantsI {
	
    /**
     * Set up the LogBack system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
            RequestArchiveService.class);
    
    /**
     * Default date format added to generated job IDs.
     */
    private static final String DATE_FORMAT = "yyyyMMdd-HH:mm:ss:SSS";
    
    /**
     * Filename extension to add to the output JSON file.
     */
    private static final String EXTENSION = ".json";
    
    /**
     * String to prepend to the front of generated job IDs. 
     */
    private static final String DEFAULT_JOB_ID = "UNAVAILABLE";
    
    /**
     * Sub-directory underneath the staging directory in which the output 
     * are stored.
     */
    private static final String DEFAULT_SUB_DIRECTORY = "debug";
    
    /**
     * Calculated path in which the request data will be stored.
     */
    private URI outputPath = null;
    
    /**
     * Default constructor. 
     */
    public RequestArchiveService() { 
        super(BundlerConstantsI.PROPERTY_FILE_NAME);
        try {
            setOutputPath(getProperty(BUNDLE_REQUEST_DIRECTORY_PROP));
            checkOutputPath();
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
     * Ensure that the output path exists.
     */
    private void checkOutputPath() {

    	if (getOutputPath() != null) {
    		Path p = Paths.get(getOutputPath());
    		if (!Files.exists(p)) {
    			try {
    				Files.createDirectory(p);
    			}
    			catch (IOException ioe) {
    				LOGGER.error("System property [ "
    						+ BUNDLE_REQUEST_DIRECTORY_PROP
    						+ " ] is set to directory [ "
    						+ getOutputPath().toString()
    						+ " ] but the directory does not exist and "
    						+ "cannot be created.  Exception message => [ "
    						+ ioe.getMessage()
    						+ " ].");
    				outputPath = null;
    			}
    		}
    	}
    	else {
    		LOGGER.info("Request archive service is disabled.");
    	}
    }
    
    /**
     * If the job ID is not supplied we'll still export the request data but
     * the job ID will be generated from the current system time.
     * 
     * @return A default job ID.
     */
    private String generateBogusJobID() {
        
        StringBuilder sb = new StringBuilder();
        DateFormat    df = new SimpleDateFormat(DATE_FORMAT);
        
        sb.append(DEFAULT_JOB_ID);
        sb.append("_");
        sb.append(df.format(System.currentTimeMillis()));
        
        return sb.toString();
    }
    
    /**
     * Method to assemble the full path to the target output file.
     * 
     * @param jobID The "main" part of the filename.
     * @return The full path to the target output file.
     */
    private URI getFilePath(String jobID) {
        
    	StringBuilder sb = new StringBuilder();
        
        if (getOutputPath() != null) {
        	sb.append(getOutputPath().toString());
            if (!sb.toString().endsWith(File.separator)) {
                sb.append(File.separator);
            }
        }
        else {
        	LOGGER.warn("Output path is not defined!");
        }
        sb.append(jobID.trim());
        sb.append(EXTENSION);
        return URIUtils.getInstance().getURI(sb.toString());
    }
    
    /**
     * Save the input String containing pretty-printed JSON data to an output 
     * file.  The output file path is calculated using the input jobID.
     * 
     * @param request The "pretty-printed" JSON data.
     * @param jobID The job ID (used to calculate the output file name)
     */
    private void saveToFile(String request, String jobID) {
        
        URI outputFile = getFilePath(jobID);
        
        if ((request != null) && (!request.isEmpty())) {
            
            LOGGER.info("Saving request information for job ID [ "
                        + jobID 
                        + " ] in file name [ "
                        + outputFile.toString()
                        + " ].");
            
            Path p = Paths.get(outputFile);
            try (BufferedWriter writer = 
            		Files.newBufferedWriter(p, Charset.forName("UTF-8"))) {
                writer.write(request);
                writer.flush();
            }
            catch (IOException ioe) {
                LOGGER.error("Unexpected IOException encountered while " 
                        + "attempting to archive the request associated with "
                        + "job ID [ "
                        + jobID 
                        + " ] in filename [ "
                        + outputFile.toString()
                        + " ].  Error message [ "
                        + ioe.getMessage()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("Unable to marshal the request data associated with "
                    + "job ID [ "
                    + jobID 
                    + " ].  The output String is null or empty.");
        }
    }
    
    /**
     * External interface used to marshal a BundleRequest into a JSON-based
     * String and then store the results in an on-disk file.
     * 
     * @param request Incoming BundleRequest object.
     * @param jobID The job ID assigned to input BundleRequest object.
     */
    @Asynchronous
    public void archiveRequest(BundleRequest request, String jobID) {
        if (getOutputPath() != null) {
	        if (request != null) {
	            if ((jobID == null) || (jobID.isEmpty())) {
	                jobID = generateBogusJobID();
	                LOGGER.warn("The input Job ID is null, or not populated.  "
	                        + "Using generated job ID [ "
	                        + jobID
	                        + " ].");
	            }
	            
	            if (LOGGER.isDebugEnabled()) {
	                LOGGER.debug("Archiving incoming request for job ID [ "
	                        + jobID
	                        + " ].");
	            }
	            
	            try { 
	                
	                ObjectMapper mapper = new ObjectMapper();
	                String requestString = 
	                        mapper.writerWithDefaultPrettyPrinter()
	                              .writeValueAsString(request);
	                saveToFile(requestString, jobID);
	                
	            }
	            catch (JsonProcessingException jpe) {
	                LOGGER.error("Unexpected JsonProcessingException encountered "
	                        + "while attempting to marshal the client supplied "
	                        + "BundleRequest object for job ID [ "
	                        + jobID
	                        + " ].  Error message [ "
	                        + jpe.getMessage()
	                        + " ].");
	            }
	        }
	        else {
	            LOGGER.error("The input BundleRequest is null.  Unable to "
	                    + "archive the incoming request information.");
	        }
        }
        else {
        	if (LOGGER.isDebugEnabled()) {
        		LOGGER.debug("Request archive feature is disabled.");
        	}
        }
    }
    
    /**
     * External interface used to marshal a BundleRequestMessage into a JSON-based
     * String and then store the results in an on-disk file.
     * 
     * @param request Incoming BundleRequestMessage object.
     * @param jobID The job ID assigned to input BundleRequestMessage object.
     */
    @Asynchronous
    public void archiveRequest(BundleRequestMessage request, String jobID) {
        if (getOutputPath() != null) {
	        if (request != null) {
	            if ((jobID == null) || (jobID.isEmpty())) {
	                jobID = generateBogusJobID();
	                LOGGER.warn("The input Job ID is null, or not populated.  "
	                        + "Using generated job ID [ "
	                        + jobID
	                        + " ].");
	            }
	            
	            if (LOGGER.isDebugEnabled()) {
	                LOGGER.debug("Archiving incoming request for job ID [ "
	                        + jobID
	                        + " ].");
	            }
	
	            String json = BundlerMessageSerializer
	                    .getInstance()
	                    .serializePretty(request);
	            saveToFile(json, jobID);
	                
	        }
	        else {
	            LOGGER.error("The input BundleRequestMessage is null.  Unable to "
	                    + "archive the incoming request information.");
	        }
        }
        else {
        	if (LOGGER.isDebugEnabled()) {
        		LOGGER.debug("Request archive feature is disabled.");
        	}
        }
    }
    
    /**
     * Getter method for the target output path.
     * 
     * @return The location to use for storing the incoming request.
     */
    private URI getOutputPath() {
        return outputPath;
    }
    
    /**
     * Setter method for the output path.  
     * 
     * @param dir Location for storing the output data.
     */
    private void setOutputPath(String dir) {
    	if ((dir != null) && (!dir.isEmpty())) { 
    		outputPath = URIUtils.getInstance().getURI(dir);
    		if (outputPath != null) {
    			LOGGER.info("Incoming requests will be archived to [ "
    					+ outputPath.toString()
    					+ " ].");
    		}
    		else {
    			LOGGER.error("System property [ "
    					+ BUNDLE_REQUEST_DIRECTORY_PROP 
    					+ " ] is set to [ "
    					+ dir 
    					+ " ] which cannot be converted to a URI.  "
    					+ "Incoming requests will not be archived.");
    		}	
    	}
    	else {
    		LOGGER.warn("Output path specified by system property [ "
    				+ BUNDLE_REQUEST_DIRECTORY_PROP
    				+ " ] is null or empty.  Request archive service "
    				+ "is disabled.");
    	}
    }
}
