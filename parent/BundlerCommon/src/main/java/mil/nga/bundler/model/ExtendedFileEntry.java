package mil.nga.bundler.model;

import java.io.Serializable;

import mil.nga.bundler.types.ArchiveType;
import mil.nga.util.URIUtils;

/**
 * Decorator class for the <code>FileEntry</code> objects that contain 
 * an estimation of the files size after compression by the appropriate 
 * algorithm.
 * 
 * @author L. Craig Carpenter
 */
public class ExtendedFileEntry 
        extends FileEntryDecorator implements Serializable {


    /**
	 * Eclipse-generated serialVersionUID
	 */
	private static final long serialVersionUID = 2772049575476679678L;

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
    public ExtendedFileEntry(ExtendedFileEntryBuilder builder) {
        super(builder.element);
        estimatedCompressedSize = builder.estimatedCompressedSize;
        type                    = builder.type;
    }
    
    /**
     * Getter method for the decorated <code>FileEntry</code> object.
     * @return The type of archive that will be generated.
     */
    @Override
    public FileEntry getFileEntry() {
        return super.getFileEntry();
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
        return getFileEntry().getEntryPath();
    }
    
    /**
     * Getter method for the full path to the target file.
     * @return The entry path.
     */
    public String getFilePath() {
        return getFileEntry().getFilePath();
    }
    
    /**
     * Getter method for the size of the target file.  
     * @return The size of the target file.
     */
    public long getSize() {
        return getFileEntry().getSize();
    }
    
    /**
     * Construct an ArchiveElement object from the input 
     * parent <code>FileEntry</code> object.
     * 
     * @return A new ArchiveElement object.
     */
    public ArchiveElement getArchiveElement() {
    	return new ArchiveElement.ArchiveElementBuilder()
    			.entryPath(getFileEntry().getEntryPath())
    			.size(getFileEntry().getSize())
    			.uri(URIUtils.getInstance()
    					.getURI(getFileEntry().getFilePath()))
    			.build();
    }
    
    /**
     * Class implementing the Builder creation pattern for new 
     * FileEntry objects.
     * 
     * @author L. Craig Carpenter
     */
    public static class ExtendedFileEntryBuilder {
        
        private FileEntry   element;
        private long        estimatedCompressedSize = -1;
        private ArchiveType type;
        
        /**
         * Method used to actually construct the ExtendedFileEntry object.
         * 
         * @return A constructed and validated ExtendedFileEntry object.
         */
        public ExtendedFileEntry build() throws IllegalStateException {
            ExtendedFileEntry object = new ExtendedFileEntry(this);
            validateExtendedFileEntryObject(object);
            return object;
        }
        
        /**
         * Setter method for the <code>FileEntry</code> object to decorate.
         * 
         * @param value The <code>FileEntry</code> object to decorate.
         * @return Reference to the parent builder object.
         */
        public ExtendedFileEntryBuilder fileEntry(FileEntry value) {
            element = value;
            return this;
        }
        
        /**
         * Setter method for the estimated compressed size of the target file. 
         * 
         * @param value The estimated compressed size of the target file.
         * @return Reference to the parent builder object.
         */
        public ExtendedFileEntryBuilder estimatedCompressedSize(long value) {
            estimatedCompressedSize = value;
            return this;
        }
        
        /**
         * Setter method for the output archive type. 
         * 
         * @param value The output archive type.
         * @return Reference to the parent builder object.
         */
        public ExtendedFileEntryBuilder type(ArchiveType value) {
            type = value;
            return this;
        }
        
        /**
         * Validate that all required fields are populated.
         * 
         * @param object The <code>ExtendedFileEntry</code> object to validate.
         * @throws IllegalStateException Thrown if any of the required fields 
         * are not populated.
         */
        public void validateExtendedFileEntryObject(ExtendedFileEntry object) 
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

