package mil.nga.bundler.interfaces;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import mil.nga.bundler.exceptions.ArchiveException;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.bundler.types.ArchiveType;

/**
 * Interface implemented by all of the archive/compressor classes.
 * 
 * @author L. Craig Carpenter
 */
public interface BundlerI {
    
    /**
     * Bundle each file in the input list.  Each entry in the list will contain
     * the URI of the target file to bundle and the path within the archive in 
     * which to place the target file.  The files will be bundled in accordance 
     * with the archive type supported by the concrete implementing class (i.e. 
     * ZIP, TAR, etc.)
     * 
     * @param files The list of files to bundle.
     * @param outputFile The target output file to create.
     * @throws IOException Raised if there are issues constructing the output
     * archive.
     */
    public void bundle(List<ArchiveElement> files, URI outputFile) 
            throws ArchiveException, IOException;
    
    
}
