package mil.nga.bundler.model;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import mil.nga.bundler.types.ArchiveType;

/**
 * Simple POJO class holding data that would be required to create a single 
 * output Archive file.  This is the simple version of the POJO.
 * 
 * @author L. Craig Carpenter
 *
 */
public class Archive implements Serializable {

    /**
     * Eclipse-generated serialVersionUID
     */
    private static final long serialVersionUID = -1399261864675805467L;
    
    /**
     * The list of files that will be included in the output archive file.
     */  
    private final List<ArchiveElement> elementList;
    
    /**
     * The archive ID.
     */
    private final int id;
    
    /**
     * The name of the output archive file.
     */  
    private final URI outputFile;
    
    /**
     * The type of output archive to generate.
     */
    private final ArchiveType type;
    
    /**
     * Constructor enforcing the Builder design pattern.
     * 
     * @param builder Builder class implementing type checking.
     */
    public Archive(ArchiveBuilder builder) {
        elementList = builder.elementList;
        id          = builder.id;
        outputFile  = builder.outputFile;
        type        = builder.type;
    }
    
    /**
     * Getter method for the list of files that will be included in the output
     * archive file.  
     * 
     * @return The list of files to include in the output archive.
     */
    public List<ArchiveElement> getElementList() {
        return elementList;
    }
    
    /**
     * Getter method for the archive ID.
     * @return The archive ID
     */
    public long getID() {
        return id;
    }
    
    /**
     * Getter method for the full path to the target output file.  
     * 
     * @return The target output archive file.
     */
    public URI getOutputFile() {
        return outputFile;
    }
    
    /**
     * Getter method for the type of archive to create.
     * 
     * @return The type of archive to create.
     */
    public ArchiveType getType() {
        return type;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Archive: ID => [ ");
        sb.append(getID());
        sb.append(" ], type => [ ");
        sb.append(getType().getText());
        sb.append(" ], output file => [ ");
        sb.append(getOutputFile().toString());
        sb.append(" ].");
        return sb.toString();
    }
    
    /**
     * Class implementing the Builder creation pattern for new 
     * ArchiveElement objects.
     * 
     * @author L. Craig Carpenter
     */
    public static class ArchiveBuilder {

        private List<ArchiveElement> elementList = new ArrayList<ArchiveElement>();
        private int                  id          = 0;
        private URI                  outputFile;
        private long                 size        = 0;
        private ArchiveType          type;
        
        /**
         * Method used to actually construct the BundlerJobMetrics object.
         * @return A constructed and validated BundlerJobMetrics object.
         */
        public Archive build() throws IllegalStateException {
            Archive object = new Archive(this);
            validateArchiveObject(object);
            return object;
        }
       
        
        /**
         * Getter method for the estimated size of the output archive.
         * @return The estimated size of the output archive.
         */
        public long getSize() {
            return size;
        }
        
        /**
         * Add an <code>ArchiveElement</code> to the internal list of elements.
         * 
         * @param value
         * @return Reference to the parent builder object.
         */
        public ArchiveBuilder element(ArchiveElement value, long size) {
            if (value != null) {
                elementList.add(value);
            }
            this.size += size;
            return this;
        }
        
        /**
         * Add an entire list to the POJO.
         * 
         * @param value The list to add to the builder.
         * @return Reference to the parent builder object.
         */
        public ArchiveBuilder elementList(List<ArchiveElement> value) {
            if ((value != null) && (!value.isEmpty())) {
                elementList = value;
            }
            return this;
        }
        
        /**
         * The ID associated with the archive.
         * 
         * @param value The ID associated with the archive.
         * @return Reference to the parent builder object.
         */
        public ArchiveBuilder id(int value) {
            id = value;
            return this;
        }
        
        /**
         * The output file to create.
         * 
         * @param value The output file to create.
         * @return Reference to the parent builder object.
         */
        public ArchiveBuilder outputFileName(URI value) {
            outputFile = value;
            return this;
        }   
        
        /**
         * Add an <code>ArchiveElement</code> to the internal list of elements.
         * 
         * @param value
         * @return Reference to the parent builder object.
         */
        public ArchiveBuilder type(ArchiveType value) {
            type = value;
            return this;
        }   
        
        /**
         * Validate that all required fields are populated.
         * 
         * @param object The ArchiveElement object to validate.
         * @throws IllegalStateException Thrown if any of the required fields 
         * are not populated.
         */
        public void validateArchiveObject(Archive object) 
                throws IllegalStateException {
            if (object.getID() < 0) {
                throw new IllegalStateException ("Invalid archive ID [ "
                        + object.getID()
                        + " ].");
            }
            if ((object.getElementList() == null) || 
                    (object.getElementList().isEmpty())) {
                throw new IllegalStateException ("List of elements to archive "
                        + "is null or empty.");
            }
            if (object.getOutputFile() == null) {
                throw new IllegalStateException ("Invalid value for output "
                        + "file URI [ null ].");
            }
            if (object.getType() == null) {
                throw new IllegalStateException ("Invalid value for output "
                        + "archive type [ null ].");
            }
        }
    }
    
    
}
