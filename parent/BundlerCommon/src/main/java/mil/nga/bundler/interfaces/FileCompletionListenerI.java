package mil.nga.bundler.interfaces;

import mil.nga.bundler.model.ArchiveElement;

/**
 * This interface is utilized by the various classes in the 
 * <code>mil.nga.bundler.archive</code> package.  It is a listener
 * interface that is called when archive processing on an individual 
 * file is completed.  This interface was added in order to provide
 * more real-time status on how an individual archive job is 
 * progressing.  
 * 
 * @author L. Craig Carpenter
 */
public interface FileCompletionListenerI {

    /**
     * Single method requiring the name of the file that was processed.
     * 
     * @param filename Object identifying the file that was updated.
     */
    public void notify(ArchiveElement element);
    
}
