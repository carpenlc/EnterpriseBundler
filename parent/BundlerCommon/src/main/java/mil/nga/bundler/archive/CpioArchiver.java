package mil.nga.bundler.archive;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.bundler.exceptions.ArchiveException;
import mil.nga.bundler.interfaces.BundlerI;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.bundler.types.ArchiveType;

/**
 * Concrete class implementing the logic to create an archive file in 
 * CPIO (i.e. tape archive) format.
 * 
 * @author L. Craig Carpenter
 */
public class CpioArchiver extends Archiver implements BundlerI {
    
    /**
     * Set up the Log4j system for use throughout the class
     */        
    final static Logger LOGGER = LoggerFactory.getLogger(CpioArchiver.class);
    
    /** 
     * The archive type handled by this class
     */
    final private ArchiveType type = ArchiveType.CPIO;
    
    /**
     * Default constructor
     */
    public CpioArchiver( ) { }
    
    /**
     * Required concrete method used to construct the type-appropriate 
     * ArchiveEntry object.
     * 
     * @param file Reference to the file to be added to the output archive.
     * @param entryPath The path within the output file where the file will be
     * placed.
     * @return The type-appropriate archive entry.
     */
    @Override
    public ArchiveEntry getArchiveEntry(URI file, String entryPath) 
            throws IOException {
        return new CpioArchiveEntry(file, entryPath);
    }
    
    /**
     * Getter method for the archive type.
     * @return The archive type that this concrete class will create.
     */
    @Override
    public ArchiveType getArchiveType() {
        return type;
    }
    
    /**
     * Execute the "bundle" operation to create a single CPIO file containing 
     * all of the required input files.
     * 
     * @param files List of files to Archive.
     * @param outputFile The output file in which the input list of files 
     * will be archived.
     * @throws ArchiveException Thrown if there are errors creating the output
     * archive file.
     * @throws IOException Thrown if there are problems accessing any of 
     * the target files. 
     */
    @Override
    public void bundle(List<ArchiveElement> files, URI outputFile) 
            throws ArchiveException, IOException {

        long startTime = System.currentTimeMillis();
        
        setOutputFile(outputFile);
        if ((files != null) && (files.size() > 0)) {
           
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating output archive file [ "
                        + getOutputFile().toString()
                        + " ].");
            }
            
            // Ensure the target output file does not already exist.
            Files.deleteIfExists(Paths.get(getOutputFile()));
            
            // Construct the output stream to the target archive file.
            try (CpioArchiveOutputStream cpioaos = 
                    new CpioArchiveOutputStream(
                            new BufferedOutputStream(
                                    Files.newOutputStream(
                                            Paths.get(getOutputFile()), 
                                            StandardOpenOption.CREATE, 
                                            StandardOpenOption.WRITE)))) {
                for (ArchiveElement element : files) {
                    cpioaos.putArchiveEntry(
                            getArchiveEntry(
                                    element.getURI(),
                                    element.getEntryPath()));
                    copyOneFile(cpioaos, element.getURI());
                    notify(element);
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Output archive [ "
                            + getOutputFile()
                            + " ] created in [ "
                            + (System.currentTimeMillis() - startTime)
                            + " ] ms.");
                }
            }
        }
        else {
            LOGGER.warn("There are no input files to process.  Output "
                    + "archive not created.");
        }
    }
}
