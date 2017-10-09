package mil.nga.bundler.model;

import java.io.Serializable;
import java.net.URI;

/**
 * Simple data structure that is used to hold the data required to bundle
 * a single file.  This class holds the target file URI, the associated 
 * path within the output archive, and the size of the file.  
 * 
 * @author L. Craig Carpenter
 */
public class ArchiveElement implements Serializable {

    /**
     * Eclipse-generated serialVersionUID
     */
    private static final long serialVersionUID = -5375301883838782257L;
    
    /**
     * URI locating the target file for bundling.
     */
    private final URI uri;
    
    /**
     * Path within the output archive where the target file will be placed.
     */
    private final String entryPath;
    
    /**
     * The size of the target file.
     */
    private final long size;
    
    /**
     * Constructor enforcing the Builder design pattern.
     * @param builder Builder class implementing type checking.
     */
    public ArchiveElement (ArchiveElementBuilder builder) {
        uri       = builder.uri;
        entryPath = builder.entryPath;
        size      = builder.size;
    }
    
    /**
     * Getter method for the Universal Resource Identifier (URI) associated 
     * with the target file.  
     * @return The URI associated with the target file.
     */
    public URI getURI() {
        return uri;
    }
    
    /**
     * Getter method for the path within the output archive in which the 
     * target file will reside.
     * @return The entry path.
     */
    public String getEntryPath() {
        return entryPath;
    }
    
    /**
     * Getter method for the size of the target file.  
     * @return The size of the target file.
     */
    public long getSize() {
        return size;
    }
    
    /**
     * Convert to a human-readable String for logging purposes.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Archive Entry => URI [ ");
        sb.append(getURI().toString());
        sb.append(" ], Entry Path [ ");
        sb.append(getEntryPath());
        sb.append(" ], size [ ");
        sb.append(getSize());
        sb.append(" ].");
        return sb.toString();
    }
    
    /**
     * Class implementing the Builder creation pattern for new 
     * ArchiveElement objects.
     * 
     * @author L. Craig Carpenter
     */
    public static class ArchiveElementBuilder {

        private URI    uri       = null;
        private String entryPath = null;
        private long   size      = -1;
        
        /**
         * Method used to actually construct the BundlerJobMetrics object.
         * @return A constructed and validated BundlerJobMetrics object.
         */
        public ArchiveElement build() throws IllegalStateException {
            ArchiveElement object = new ArchiveElement(this);
            validateArchiveElementObject(object);
            return object;
        }
        
        /**
         * Setter method for the URI of the target file to bundle.
         * 
         * @param value The URI requested.
         * @return Reference to the parent builder object.
         */
        public ArchiveElementBuilder uri(URI value) {
            uri = value;
            return this;
        }
        
        /**
         * Setter method for the path in the output archive in which the 
         * target file will be placed.
         * 
         * @param value The archive entry path.
         * @return Reference to the parent builder object.
         */
        public ArchiveElementBuilder entryPath(String value) {
            entryPath = value;
            return this;
        }
        
        /**
         * Setter method for the size of the target file. 
         * 
         * @param value The size of the target file.
         * @return Reference to the parent builder object.
         */
        public ArchiveElementBuilder size(long value) {
            size = value;
            return this;
        }
        
        /**
         * Validate that all required fields are populated.
         * 
         * @param object The ArchiveElement object to validate.
         * @throws IllegalStateException Thrown if any of the required fields 
         * are not populated.
         */
        public void validateArchiveElementObject(ArchiveElement object) 
                throws IllegalStateException {
            if (object.getURI() == null) {
                throw new IllegalStateException ("Invalid value for target "
                        + "URI [ null ].");
            }
            if ((object.getEntryPath() == null) || 
                    (object.getEntryPath().isEmpty())) {
                throw new IllegalStateException ("Invalid value for entry "
                        + "path [ "
                        + object.getEntryPath()
                        + " ].");
            }
            if (object.getSize() < 0) {
                throw new IllegalStateException ("Invalid value for file "
                        + "size [ "
                        + object.getSize()
                        + " ].");
            }
        }
    }
}
