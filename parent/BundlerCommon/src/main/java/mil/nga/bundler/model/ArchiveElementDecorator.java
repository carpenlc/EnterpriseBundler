package mil.nga.bundler.model;

import java.io.Serializable;

/**
 * Implementation of Decorator pattern for <code>ArchiveElement</code> 
 * objects.
 * 
 * @author L. Craig Carpenter
 */
public class ArchiveElementDecorator implements Serializable {

    /**
     * Eclipse-generated serialVersionUID
     */
    private static final long serialVersionUID = 4152521537447382659L;
    
    /**
     * <code>ArchiveElement</code> object to decorate.
     */
    protected ArchiveElement element;
    
    /**
     * Default constructor requiring a reference to the parent
     * <code>ArchiveElement</code> object.
     * 
     * @param value The <code>ArchiveElement</code> object that we want to 
     * decorate.
     */
    public ArchiveElementDecorator(ArchiveElement value) {
        element = value;
    }
    
    /**
     * Getter method for the <code>ArchiveElement</code> to be decorated.
     * @return The decorated <code>ArchiveElement</code> object.
     */
    protected ArchiveElement getArchiveElement() {
        return element;
    }
    
}
