package mil.nga.bundler.model;

import java.io.Serializable;

/**
 * Implementation of Decorator pattern for <code>FileEntry</code> 
 * objects.
 * 
 * @author L. Craig Carpenter
 */
public class FileEntryDecorator implements Serializable {
    
    /**
	 * Eclipse-generated serialVersionUID
	 */
	private static final long serialVersionUID = 5061739379976688490L;

	/**
     * <code>FileEntry</code> object to decorate.
     */
    protected FileEntry element;
    
    /**
     * Default constructor requiring a reference to the parent
     * <code>FileEntry</code> object.
     * 
     * @param value The <code>FileEntry</code> object that we want to 
     * decorate.
     */
    public FileEntryDecorator(FileEntry value) {
        element = value;
    }
    
    /**
     * Getter method for the <code>FileEntry</code> to be decorated.
     * @return The decorated <code>FileEntry</code> object.
     */
    protected FileEntry getFileEntry() {
        return element;
    }
    
}

