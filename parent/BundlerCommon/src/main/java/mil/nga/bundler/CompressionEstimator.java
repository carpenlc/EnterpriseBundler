package mil.nga.bundler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.bundler.types.ArchiveType;
import mil.nga.bundler.model.ExtendedArchiveElement;
import mil.nga.bundler.model.ExtendedFileEntry;
import mil.nga.bundler.model.FileEntry;

/**
 * In order to create an output archive that is close to the requested output
 * size requested, we have to know how much a given file will compress.  
 * Unfortunately, we don't know how much a given file will compress until we 
 * compress it.  This class relies on historical data to come up with a 
 * reasonable estimate of how much a file will compress based on extension 
 * and file type.
 * 
 * TODO: Need to actually implement the algorithm.
 * 
 * @author L. Craig Carpenter
 */
public class CompressionEstimator implements BundlerConstantsI {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    static final Logger LOGGER = LoggerFactory.getLogger(
            CompressionEstimator.class);
    
    
    /**
     * Getter method for the singleton instance of the CompressionEstimator.
     * @return Handle to the singleton instance of the CompressionEstimator.
     */
    public static CompressionEstimator getInstance() {
        return CompressionEstimatorHolder.getFactorySingleton();
    }
    
    /**
     * This method will loop through a list of <code>ArchiveElement</code> 
     * objects estimating the compressed size of each file.  The results will 
     * be returned in a list of <code>ExtendedArchiveElement</code> objects.
     * 
     * @param fileList List of files to estimate the compressed size of.
     * @param type Type of compression algorithm.
     * @return A list of decorated <code>ArchiveElement</code> objects.
     */
    public List<ExtendedFileEntry> getEstimatedCompressedSizeForFileEntry(
            List<FileEntry> fileList,
            ArchiveType type) {
        
        long startTime = System.currentTimeMillis();
        List<ExtendedFileEntry> extendedList = 
                new ArrayList<ExtendedFileEntry>();
        
        if ((fileList != null) && (!fileList.isEmpty())) { 
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Generating estimated compressed file sizes for " 
                        + "list of [ "
                        + fileList.size()
                        + " ] objects.");
            }
            
            for (FileEntry element : fileList) {
                try {
                    extendedList.add(
                                new ExtendedFileEntry.ExtendedFileEntryBuilder()
                                    .fileEntry(element)
                                    .type(type)
                                    .estimatedCompressedSize(
                                            getEstimatedCompressedFileSize(
                                                    element, 
                                                    type))
                                    .build()
                            );
                }
                catch (IllegalStateException ise) {
                    LOGGER.warn("Unexpected IllegalStateException raised "
                            + "while calculating estimated compressed "
                            + "file sizes.  Exception message => [ "
                            + ise.getMessage()
                            + " ].");
                }
            }
        }
        else {
            LOGGER.warn("Input file list was null or empty.  Output file "
                    + "list will be empty.");
        }
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Estimation of compressed file sizes completed in [ "
                    + (System.currentTimeMillis() - startTime)
                    + " ] ms.");
        }
        
        return extendedList;
    }
    
    /**
     * This method will loop through a list of <code>ArchiveElement</code> 
     * objects estimating the compressed size of each file.  The results will 
     * be returned in a list of <code>ExtendedArchiveElement</code> objects.
     * 
     * @param fileList List of files to estimate the compressed size of.
     * @param type Type of compression algorithm.
     * @return A list of decorated <code>ArchiveElement</code> objects.
     */
    public List<ExtendedArchiveElement> getEstimatedCompressedSize(
            List<ArchiveElement> fileList,
            ArchiveType type) {
        
        long startTime = System.currentTimeMillis();
        List<ExtendedArchiveElement> extendedList = 
                new ArrayList<ExtendedArchiveElement>();
        
        if ((fileList != null) && (!fileList.isEmpty())) { 
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Generating estimated compressed file sizes for " 
                        + "list of [ "
                        + fileList.size()
                        + " ] objects.");
            }
            
            for (ArchiveElement element : fileList) {
                try {
                    extendedList.add(
                                new ExtendedArchiveElement.ExtendedArchiveElementBuilder()
                                    .archiveElement(element)
                                    .type(type)
                                    .estimatedCompressedSize(
                                            getEstimatedCompressedFileSize(
                                                    element, 
                                                    type))
                                    .build()
                            );
                }
                catch (IllegalStateException ise) {
                    LOGGER.warn("Unexpected IllegalStateException raised "
                            + "while calculating estimated compressed "
                            + "file sizes.  Exception message => [ "
                            + ise.getMessage()
                            + " ].");
                }
            }
        }
        else {
            LOGGER.warn("Input file list was null or empty.  Output file "
                    + "list will be empty.");
        }
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Estimation of compressed file sizes completed in [ "
                    + (System.currentTimeMillis() - startTime)
                    + " ] ms.");
        }
        
        return extendedList;
    }
    
    /**
     * Calculate an estimate of the size of the output archive file.
     * 
     * @param file Data about the file to be compressed.
     * @param type The type of archive to create.
     * @return The estimated size of the output archive with a file of 
     * the input size added.
     */
    public long getEstimatedCompressedFileSize(
            FileEntry   file,
            ArchiveType type) {
        
        double estimatedSize = 0.0;
        
        if ((file != null) && (file.getSize() > 0)) {
            double multiplier = 
                    (100.0 - AVERAGE_COMPRESSION_PERCENTAGE) / 100.0;
            estimatedSize = multiplier * (double)(file.getSize());
        }
        else {
            LOGGER.warn("Null ArchiveElement received.  Returned estimated "
                    + "size will be [ 0 ].");
        }
        return (long)estimatedSize;
    }
    
    /**
     * Calculate an estimate of the size of the output archive file.
     * 
     * @param file Data about the file to be compressed.
     * @param type The type of archive to create.
     * @return The estimated size of the output archive with a file of 
     * the input size added.
     */
    public long getEstimatedCompressedFileSize(
            ArchiveElement file,
            ArchiveType    type) {
        
        double estimatedSize = 0.0;
        
        if ((file != null) && (file.getSize() > 0)) {
            double multiplier = 
                    (100.0 - AVERAGE_COMPRESSION_PERCENTAGE) / 100.0;
            estimatedSize = multiplier * (double)(file.getSize());
        }
        else {
            LOGGER.warn("Null ArchiveElement received.  Returned estimated "
                    + "size will be [ 0 ].");
        }
        return (long)estimatedSize;
    }
    
    /** 
     * Static inner class used to construct the factory singleton.  This
     * class exploits that fact that inner classes are not loaded until they 
     * referenced therefore enforcing thread safety without the performance 
     * hit imposed by the use of the "synchronized" keyword.
     * 
     * @author L. Craig Carpenter
     */
    public static class CompressionEstimatorHolder {
        
        /**
         * Reference to the Singleton instance of the factory
         */
        private static CompressionEstimator factory = new CompressionEstimator();
        
        /**
         * Accessor method for the singleton instance of the factory object.
         * @return The singleton instance of the factory.
         */
        public static CompressionEstimator getFactorySingleton() {
            return factory;
        }
    }
}
