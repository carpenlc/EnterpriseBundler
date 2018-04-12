package mil.nga.bundler.archive;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.List;

import mil.nga.bundler.interfaces.BundlerI;
import mil.nga.bundler.exceptions.ArchiveException;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.bundler.types.ArchiveType;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of the Bundler class that will handle creation 
 * of an output compressed BZip2 archive.  This class is used in conjunction 
 * with the TAR archiver.  Compression is a two-step process.  First, the 
 * intermediate TAR archive is created, then the output TAR archive is run 
 * through BZip2 compressor.  
 * 
 * This compressor seems to be ever-so-slightly slower than the other compression
 * algorithms, but the output has better compression.
 * 
 * @author L. Craig Carpenter
 */
public class BZip2Archiver 
        extends Compressor implements BundlerI {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    final static Logger LOGGER = LoggerFactory.getLogger(BZip2Archiver.class);
    
    /** 
     * The archive type handled by this class
     */
    private ArchiveType _type = ArchiveType.BZIP2;
    
    /**
     * Default constructor
     */
    public BZip2Archiver() { }
    
    /**
     * Compress the data contained in the input file using the BZip2 
     * compression algorithms storing the compressed data in the file
     * specified by the outputFile parameter. 
     * 
     * @param inputFile The input TAR archive
     * @param outputFile The compressed output file.
     */
    @Override
    public void compress(URI inputFile, URI outputFile) 
            throws IOException {
        
        try (
            // s3fs file system provider does not currently support OpenOptions.
            // work around by not supplying them.
            //BufferedInputStream bIn = new BufferedInputStream(
            //        Files.newInputStream(
            //                Paths.get(inputFile),
            //                StandardOpenOption.READ));
            BufferedInputStream bIn = new BufferedInputStream(
                      Files.newInputStream(Paths.get(inputFile)));
            BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(
                    Files.newOutputStream(
                            Paths.get(outputFile), 
                            StandardOpenOption.CREATE, 
                            StandardOpenOption.WRITE))) {
            // Pipe the input stream to the output stream
            compress(bIn, bzOut);
        }
    }
    
    /**
     * Implementation of BundlerI interface.  It is responsible for driving 
     * the creation of the output compressed file.  
     * 
     * @param files List of files to Archive.
     * @param outputFile The output file in which the input list of files 
     * will be archived.
     * @throws ArchiveException Thrown if there are problems creating either
     * of the output archive files.
     * @throws IOException Thrown if there are problems accessing any of the
     * target files.
     */
    @Override
    public void bundle(List<ArchiveElement> files, URI outputFile) 
            throws ArchiveException, IOException {
        
        long initialSize    = -1;
        long compressedSize = -1;
        long startTime      = System.currentTimeMillis();
        
        LOGGER.info(_type.getText() + " : Creating intermediate TAR file.");
        super.bundle(files, outputFile);
        
        // Save a handle to the intermediate TAR file.
        URI intermediateTARFile = getOutputFile();
        if (Files.exists(Paths.get(intermediateTARFile))) {
            initialSize = Files.size(Paths.get(intermediateTARFile));
            LOGGER.info(_type.getText() 
                    + " : Intermediate TAR file created successfully.  File "
                    + "created [ " 
                    + intermediateTARFile.toString()
                    + " ]. Creating compressed output file.");
            compress(intermediateTARFile, _type);
            
            if (Files.exists(Paths.get(getOutputFile()))) {
                compressedSize = Files.size(Paths.get(getOutputFile()));
            }
            else {
                LOGGER.error(_type.getText()
                        + " : Unknown error occurred during compression.  Target "
                        + "output file [ "
                        + getOutputFile().toString()
                        + " ] does not exist.");
            }
        }
        
        // Output the amount of compression obtained.
        if ((initialSize > 0) && (compressedSize > 0)) {
            double percentCompressed = ((double)initialSize - (double)compressedSize) /
                    (double)initialSize;
            DecimalFormat df = new DecimalFormat("##.##%");
            LOGGER.info(_type.getText()
                    + " : Output compressed file created [ "
                    + getOutputFile().toString()
                    + " ].  Compression percentage obtained [ "
                    + df.format(percentCompressed)
                    + " ].");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(_type.getText()
                        + " : Output compressed file [ "
                        + getOutputFile().toString()
                        + " ] created in [ "
                        + (System.currentTimeMillis() - startTime)
                        + " ] ms.");
            }
        }
    }
}
