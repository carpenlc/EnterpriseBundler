package mil.nga.bundler.model;

import java.io.Serializable;
import java.net.URI;

import mil.nga.bundler.types.ArchiveType;

/**
 * Decorator class for the <code>ArchiveElement</code> objects that contain 
 * an estimation of the files size after compression by the appropriate 
 * algorithm.
 * 
 * @author L. Craig Carpenter
 */
public class ExtendedArchiveElement 
        extends ArchiveElementDecorator implements Serializable {

    /**
     * Eclipse-generated serialVersionUID
     */
    private static final long serialVersionUID = -629980076283534918L;

    /**
     * Estimated compressed size of parent file.
     */
    private final long estimatedCompressedSize;
    
    /**
     * The type of output archive is an important factor in determining the 
     * output compressed size.
     */
    private final ArchiveType type;
    
    /**
     * Constructor enforcing the Builder design pattern.
     * @param builder Builder class implementing type checking.
     */
    public ExtendedArchiveElement(ExtendedArchiveElementBuilder builder) {
        super(builder.element);
        estimatedCompressedSize = builder.estimatedCompressedSize;
        type                    = builder.type;
    }
    
    /**
     * Getter method for the decorated <code>ArchiveElement</code> object.
     * @return The type of archive that will be generated.
     */
    @Override
    public ArchiveElement getArchiveElement() {
        return super.getArchiveElement();
    }
    
    /**
     * Getter method for the type of output archive.
     * @return The type of archive that will be generated.
     */
    public ArchiveType getArchiveType() {
        return type;
    }
    
    /**
     * Getter method for the estimated compressed size of the parent file.
     * @return The estimated compressed size.
     */
    public long getEstimatedCompressedSize() {
        return estimatedCompressedSize;
    }
    
    /**
     * Getter method for the path within the output archive in which the 
     * target file will reside.
     * @return The entry path.
     */
    public String getEntryPath() {
        return getArchiveElement().getEntryPath();
    }
    
    /**
     * Getter method for the size of the target file.  
     * @return The size of the target file.
     */
    public long getSize() {
        return getArchiveElement().getSize();
    }
    
    /**
     * Getter method for the Universal Resource Identifier (URI) associated 
     * with the target file.  
     * @return The URI associated with the target file.
     */
    public URI getURI() {
        return getArchiveElement().getURI();
    }
    
    /**
     * Class implementing the Builder creation pattern for new 
     * ArchiveElement objects.
     * 
     * @author L. Craig Carpenter
     */
    public static class ExtendedArchiveElementBuilder {
        
        private ArchiveElement element;
        private long          estimatedCompressedSize = -1;
        private ArchiveType    type;
        
        /**
         * Method used to actually construct the ExtendedArchiveElement object.
         * 
         * @return A constructed and validated ExtendedArchiveElement object.
         */
        public ExtendedArchiveElement build() throws IllegalStateException {
            ExtendedArchiveElement object = new ExtendedArchiveElement(this);
            validateExtendedArchiveElementObject(object);
            return object;
        }
        
        /**
         * Setter method for the <code>ArchiveElement</code> object to decorate.
         * 
         * @param value The <code>ArchiveElement</code> object to decorate.
         * @return Reference to the parent builder object.
         */
        public ExtendedArchiveElementBuilder archiveElement(ArchiveElement value) {
            element = value;
            return this;
        }
        
        /**
         * Setter method for the estimated compressed size of the target file. 
         * 
         * @param value The estimated compressed size of the target file.
         * @return Reference to the parent builder object.
         */
        public ExtendedArchiveElementBuilder estimatedCompressedSize(long value) {
            estimatedCompressedSize = value;
            return this;
        }
        
        /**
         * Setter method for the output archive type. 
         * 
         * @param value The output archive type.
         * @return Reference to the parent builder object.
         */
        public ExtendedArchiveElementBuilder type(ArchiveType value) {
            type = value;
            return this;
        }
        
        /**
         * Validate that all required fields are populated.
         * 
         * @param object The <code>ExtendedArchiveElement</code> object to validate.
         * @throws IllegalStateException Thrown if any of the required fields 
         * are not populated.
         */
        public void validateExtendedArchiveElementObject(ExtendedArchiveElement object) 
                throws IllegalStateException {
            if (object.getArchiveType() == null) {
                throw new IllegalStateException ("Invalid value for archive "
                        + "type [ null ].");
            }
            if (object.getEstimatedCompressedSize() < 0) {
                throw new IllegalStateException ("Invalid value for estimated "
                        + "compressed file size [ "
                        + object.getEstimatedCompressedSize()
                        + " ].");
            }
        }
    }
    
}
