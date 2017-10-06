package mil.nga.bundler.archive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import mil.nga.bundler.types.ArchiveType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract intermediate class used in conjunction with the compression-based
 * algorithms that rely on the creation of an intermediate archive before 
 * proceeding with compression.  
 * 
 * @author L. Craig Carpenter
 */
public abstract class Compressor extends TarArchiver {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    final static Logger LOGGER = LoggerFactory.getLogger(Compressor.class);
    
    /**
     * Buffer size to use when creating the output compressed file.
     * TODO: Tweak this value for better performance.
     */
    protected static final int BUFFER_SIZE = 8192;
    
    /**
     * Default no-arg constructor
     */
    public Compressor() { }
    
    /**
     * This method takes an InputStream and pipes it through a compressor 
     * algorithm.  The specific algorithm used is determined by subclasses.
     * 
     * @param in BufferedInputStream associated with the input TAR file to
     * be compressed.  
     * @param out Concrete implementation of an output Compressor.
     * @throws IOException Thrown if there are problems with any of processing
     * associated with reading or writing the data in the pipe operation. 
     */
    public void compress(BufferedInputStream in, OutputStream out) 
            throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        int n = 0;
        while (-1 != (n = in.read(buffer))) {
            out.write(buffer, 0, n);
        }
    }
    
    /**
     * Orchestration method that drives the generation of the final output
     * compressed file.
     * 
     * @param tarFile Intermediate TAR file.
     * @param type Compression algorithm to use in creating the output 
     * compressed file.
     * @throws IOException Thrown if there are errors interacting with the
     * file system.
     */
    protected void compress(URI tarFile, ArchiveType type) 
            throws IOException {
        
        URI compressedFile = null;
        
        // Reset the output archive name
        setOutputFile(tarFile, type.getText());
        compressedFile = getOutputFile();
        
        // Compress the tar file.
        compress(tarFile, compressedFile);

        // Check that the output compressed file was created, then
        // delete the intermediate TAR file.
        if (Files.exists(Paths.get(compressedFile))) {
            LOGGER.info("Compressed file created successfully.  "
                    + "File created [ " 
                    + compressedFile.toString()
                    + " ] Deleting the intermediate TAR file.");
            Files.deleteIfExists(Paths.get(tarFile));
        }
        else {
            LOGGER.error("Output compressed file does not exist.  Unknown "
                    + "error encountered.");
        }
    }
    
    /**
     * Compress the data contained in the input file using the specified 
     * compression algorithms storing the compressed data in the file
     * specified by the outputFile parameter. 
     * 
     * @param inputFile The input TAR archive
     * @param outputFile The compressed output file.
     */
    public abstract void compress(URI inputFile, URI outputFile) throws 
            IOException;
}
