package mil.nga.bundler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import mil.nga.PropertyLoader;
import mil.nga.bundler.exceptions.PropertiesNotLoadedException;
import mil.nga.bundler.interfaces.BundlerConstantsI;

/**
 * This class is responsible for ensuring that all required NIO2 file systems
 * are available.
 * 
 * @author L. Craig Carpenter
 */
public class FileSystemFactory 
        extends PropertyLoader 
        implements BundlerConstantsI {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    static final Logger LOGGER = LoggerFactory.getLogger(
            FileSystemFactory.class);
    
    /**
     * The default endpoint for AWS S3.
     */
    private static final String DEFAULT_S3_ENDPOINT = "s3.amazonaws.com";
    
    /**
     * Flag indicating whether or not the S3 file system has been loaded.  This 
     * was added to ensure that in any application the file system is loaded 
     * once and only once.
     */
    private static boolean s3FileSystemLoaded = false;
    
    /**
     * The IAM role that will be used to for authentication to AWS.
     */
    private String iamRole;
    
    /**
     * This is the default host name for the s3 end-point.  
     */
    private String s3EndPoint;
    
    /**
     * Default constructor used to load the required properties.
     */
    private FileSystemFactory() { 
        super(PROPERTY_FILE_NAME);
        
        String iamRole    = null;
        String s3EndPoint = null;
        
        try {
        	iamRole    = getProperty(IAM_ROLE_PROPERTY);
        	s3EndPoint = getProperty(S3_END_POINT_PROPERTY);
        }
        catch (PropertiesNotLoadedException pnle) {
            LOGGER.warn("An unexpected PropertiesNotLoadedException " 
                    + "was encountered.  Please ensure the application "
                    + "is properly configured.  Exception message [ "
                    + pnle.getMessage()
                    + " ].");
        }
        setIAMRole(iamRole);
        setS3EndPoint(s3EndPoint);
    } 
    
    
    /**
     * Method to list the available <code>FileSystemProvider</code> objects.
     */
    public void listFileSystemsAvailable() {
        List<FileSystemProvider> availableProviders = 
                FileSystemProvider.installedProviders();
        for (FileSystemProvider provider : availableProviders) {
            LOGGER.info("Provider scheme => " + provider.getScheme());
        }
    }
    
    /**
     * Ensure that the S3 FileSystem provider is loaded and available.
     */
    public void loadS3Filesystem() {
        
    	String uriString = "s3://" + getS3EndPoint() + "/";
    	
        // Ensure the s3 filesystem is not already loaded.
        if (!s3FileSystemLoaded) {
            if ((getIAMRole() != null) && 
                    (!getIAMRole().isEmpty())) {
                
                Map<String, ?> env = ImmutableMap.<String, Object> builder()
                        .put(com.upplication.s3fs.AmazonS3Factory.IAM_ROLE, 
                            getIAMRole())
                        .build();
        
                try {
                    
                	if (LOGGER.isDebugEnabled()) {
                		LOGGER.debug("Initializing S3 filesystem with IAM "
                				+ "role [ "
                				+ getIAMRole()
                				+ " ] and URI [ "
                				+ uriString
                				+ " ].");
                	}
                	
                    // Add the s3 file system provider
                    FileSystems.newFileSystem(
                        new URI(uriString), 
                        env, 
                        Thread.currentThread().getContextClassLoader());
                    
                    // If no exceptions, the file system provider was loaded 
                    // successfully.
                    s3FileSystemLoaded = true;
                    
                    LOGGER.info("NIO 2 s3 FileSystem initialized successfully.");
                    
                } 
                catch (IOException ioe) {
                    LOGGER.error("Unexpected IOException while loading the "
                            + "S3 filesystem provider.  Exception "
                            + "message => [ "
                            + ioe.getMessage()
                            + " ].");
                }
                // URISyntaxException cannot happen because we're converting a 
                // static String.  Just eat the exception.
                catch (URISyntaxException use) { }
            }
            else {
                LOGGER.warn("The IAM role defined by property [ "
                        + IAM_ROLE_PROPERTY
                        + " ] is not available.");
            }
        }
        else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("s3 file system provider already loaded.");
            }
        }
    }
    
    /**
     * Getter method for the IAM Role that will be used for authentication to
     * AWS.
     * @return The IAM Role to use.
     */
    public String getIAMRole() {
        return iamRole;
    }
    
    /** 
     * Getter method for the target S3 end-point.
     * @return The target S3 end-point.
     */
    public String getS3EndPoint() {
    	return s3EndPoint;
    }
    
    /**
     * Getter method for the singleton instance of the FileSystemFactory.
     * @return Handle to the singleton instance of the FileSystemFactory.
     */
    public static FileSystemFactory getInstance() {
        return FileSystemFactoryHolder.getFactorySingleton();
    }
    
    /**
     * Setter method for the IAM Role that will be used for authentication to
     * AWS.
     * @param value The IAM Role to use.
     */
    public void setIAMRole(String value) {
        iamRole = value;
    }
    
    /**
     * Setter method for the S3 end-point that will be used for construction 
     * of the file system.
     * @param value The target S3 end-point.
     */
    public void setS3EndPoint(String value) {
    	if ((value == null) || (value.isEmpty())) {
    		s3EndPoint = DEFAULT_S3_ENDPOINT;
    	}
    	else {
    		s3EndPoint = value.trim();
    	}
    }
    
    /** 
     * Static inner class used to construct the factory singleton.  This
     * class exploits that fact that inner classes are not loaded until they 
     * referenced therefore enforcing thread safety without the performance 
     * hit imposed by the use of the "synchronized" keyword.
     * 
     * @author L. Craig Carpenter
     */
    public static class FileSystemFactoryHolder {
        
        /**
         * Reference to the Singleton instance of the factory
         */
        private static FileSystemFactory factory = new FileSystemFactory();
        
        /**
         * Accessor method for the singleton instance of the factory object.
         * @return The singleton instance of the factory.
         */
        public static FileSystemFactory getFactorySingleton() {
            return factory;
        }
    }
    
    public static void main(String[] args) {
        FileSystemFactory.getInstance().listFileSystemsAvailable();
    }
}
