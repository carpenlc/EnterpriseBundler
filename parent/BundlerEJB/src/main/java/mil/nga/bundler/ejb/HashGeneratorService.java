package mil.nga.bundler.ejb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import mil.nga.bundler.types.HashType;
import mil.nga.util.URIUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session Bean implementation class HashGeneratorService
 * 
 * This EJB is invoked after the archive files are created.  It will 
 * construct a hash of the supplied input file.  This service is also used 
 * by the validation algorithm generating a hash that can be used to 
 * uniquely identify a file and whether or not it has changed since a 
 * previous execution.
 * 
 * Note: We switched to using the commons codec classes because we found 
 * issues when converting the output hashes to Base64 using the JDK classes 
 * (specifically, leading 0s were being dropped).
 * 
 * @author L. Craig Carpenter
 */
@Stateless
@LocalBean
public class HashGeneratorService {

    /**
     * Set up the Log4j system for use throughout the class
     */
    static final Logger LOGGER = LoggerFactory.getLogger(HashGeneratorService.class);
    
    /**
     * Default constructor. 
     */
    public HashGeneratorService() { }
    
    /**
     * Construct the hexadecimal-based hash of the input file.  If the input
     * file doesn't exist, or errors are encountered during hash generation the 
     * returned hash is null.
     * 
     * @param inputFile String containing the full path to a file on which
     * the requested hash should be applied.
     * @param hashType The type of hash to create 
     * @see <code>mil.nga.bundler.types.HashType</code>
     * @return The hash value as a hex string.
     */
    public String getHash(URI uri, HashType hashType) {
        
        String hash = null;

        if (uri != null) { 
            if (Files.exists(Paths.get(uri))) {
                
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Generating [ "
                            + hashType.getText()
                            + " ] hash for file [ "
                            + uri.toString()
                            + " ].");
                }
                
                long startTime = System.currentTimeMillis();
                switch (hashType) {
                    case MD5 : 
                        hash = getMD5Hash(uri);
                        break;
                    case SHA1:
                        hash = getSHA1Hash(uri);
                        break;
                    case SHA256:
                        hash = getSHA256Hash(uri);
                        break;
                    case SHA384:
                        hash = getSHA384Hash(uri);                        
                        break;
                    case SHA512:
                        hash = getSHA512Hash(uri);                        
                        break;
                }
                
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                           "Hash type [ "
                            + hashType.getText()
                            + " ] for file [ "
                            + uri.toString()
                            + " ] created in [ "
                            + Long.toString(elapsedTime)
                            + " ] ms.");
                }
            }
            else {
                LOGGER.error("Input file does not exists.  Input file "
                        + "specified [ "
                        + uri.toString()
                        + " ].");
            }
        }
        else {
            LOGGER.error("The require input Path parameter is null or empty. "
                    + " The output hash file will not be generated.");
        }
        return hash;
    }
    
    /**
     * Generate a SHA-1 hash associated with the input 
     * 
     * @param inputFile The file on which to generate a hash.
     * @return The generated hash.
     * @deprecated
     * Use <code>getHash(String, HashType)</code>
     */
    public String generate(String inputFile) {
        URI uri = URIUtils.getInstance().getURI(inputFile);
        return getHash(uri, HashType.SHA1);
    }
    
    /**
     * Generate a hash associated with the input file and store it in the output
     * file.
     * 
     * @param inputFile The file on which to generate a hash.
     * @param outputFile The output file in which to store the generated hash.
     */
    public void generate(String inputFile, String outputFile) {
        
        String method = "generate() - ";

        if ((inputFile != null) && (!inputFile.isEmpty())) {
            if ((outputFile != null) && (!outputFile.isEmpty())) {
                
                URI input  = URIUtils.getInstance().getURI(inputFile);
                URI output = URIUtils.getInstance().getURI(outputFile);
                
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Creating hash for file [ "
                            + input.toString() 
                            + " ].");
                }
                
                if (Files.exists(Paths.get(input))) {
                    String hash = getHash(input, HashType.SHA1);
                    saveHash(hash, Paths.get(output));
                }
                else {
                    LOGGER.error(method
                            + "Input file does not exists.  Input file "
                            + "specified [ "
                            + inputFile
                            + " ].");
                }
            }
            else {
                    LOGGER.error(method
                            + "The require input file parameter is null or empty. "
                            + " The output hash file will not be generated.");
            }
        }
        else {
            LOGGER.error(method
                    + "The require input file parameter is null or empty.  "
                    + "The hash will not be generated.");
        }
    }
    
    /**
     * Calculate the MD5 hash using the Apache Commons Codec classes.  
     * 
     * @param file The file we need the hash for.
     * @return The calculated MD5 hash.
     */
    public String getMD5Hash(URI file) {

        String hash = null;

        if (file != null) {
            try (InputStream is = Files.newInputStream(Paths.get(file))) {
                hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
            }
            catch (IOException ioe) {
                LOGGER.error(
                     "Unexpected IOException encountered while generating "
                     + "the [ " 
                     + HashType.MD5.getText() 
                     + " ] hash for file [ "
                     + file.toString()
                     + " ].  Exception message [ "
                     + ioe.getMessage()
                     + " ].  Method will return a null hash.");
            }
        }
        else {
            LOGGER.error("Input file Path is null.  Hash will not be generated.");
        }
        return hash;
    }
    
    /**
     * Calculate the SHA-1 hash using the Apache Commons Codec classes.
     * Note: SHA-1 hash generation seems to take about twice as long as MD5 
     * hash generation.
     * 
     * @param file The file we need the hash for.
     * @return The calculated SHA1 hash.
     */
    public String getSHA1Hash(URI file) {

        String          hash = null;
        if (file != null) {
            try (InputStream is = Files.newInputStream(Paths.get(file))) {
                hash = org.apache.commons.codec.digest.DigestUtils.sha1Hex(is);
            }
            catch (IOException ioe) {
                 LOGGER.error(
                         "Unexpected IOException encountered while generating "
                         + "the [ " 
                         + HashType.SHA1.getText() 
                         + " ] hash for file [ "
                         + file.toString()
                         + " ].  Exception message [ "
                         + ioe.getMessage()
                         + " ].  Method will return a null hash.");
            }
        }
        else {
            LOGGER.error("Input file Path is null.  Hash will not be generated.");
        }
        return hash;
    }

    /**
     * Calculate the SHA-256 hash using the Apache Commons Codec classes.
     * 
     * @param file The file we need the hash for.
     * @return The calculated SHA256 hash.
     */
    public String getSHA256Hash(URI file) {

        String hash = null;
        
        if (file != null) {
            try (InputStream is = Files.newInputStream(Paths.get(file))) {
                hash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(is);
            }
            catch (IOException ioe) {
                LOGGER.error(
                    "Unexpected IOException encountered while generating "
                    + "the [ " 
                    + HashType.SHA256.getText() 
                    + " ] hash for file [ "
                    + file.toString()
                    + " ].  Exception message [ "
                    + ioe.getMessage()
                    + " ].  Method will return a null hash.");
            }
        }
        else {
            LOGGER.error("Input file Path is null.  Hash will not be generated.");
        }
        return hash;
    }
    
    /**
     * Calculate the SHA-384 hash using the Apache Commons Codec classes.
     * 
     * @param file The file we need the hash for.
     * @return The calculated SHA384 hash.
     */
    public String getSHA384Hash(URI file) {

        String hash = null;

        if (file != null) {
            try (InputStream is = Files.newInputStream(Paths.get(file))) {
                hash = org.apache.commons.codec.digest.DigestUtils.sha384Hex(is);
            }
            catch (IOException ioe) {
                LOGGER.error(
                    "Unexpected IOException encountered while generating "
                    + "the [ " 
                    + HashType.SHA384.getText() 
                    + " ] hash for file [ "
                    + file.toString()
                    + " ].  Exception message [ "
                    + ioe.getMessage()
                    + " ].  Method will return a null hash.");
            }
        }
        else {
            LOGGER.error("Input file Path is null.  Hash will not be generated.");
        }
        
        return hash;
    }
    
    /**
     * Calculate the SHA-512 hash using the Apache Commons Codec classes.
     * 
     * @param file The file we need the hash for.
     * @return The calculated SHA512 hash.
     */
    public String getSHA512Hash(URI file) {

        String hash = null;

        if (file != null) {
            try (InputStream is = Files.newInputStream(Paths.get(file))) {
                hash = org.apache.commons.codec.digest.DigestUtils.sha512Hex(is);
            }
            catch (IOException ioe) {
                LOGGER.error(
                    "Unexpected IOException encountered while generating "
                    + "the [ " 
                    + HashType.SHA512.getText() 
                    + " ] hash for file [ "
                    + file.toString()
                    + " ].  Exception message [ "
                    + ioe.getMessage()
                    + " ].  Method will return a null hash.");
            }
        }
        else {
            LOGGER.error("Input file Path is null.  Hash will not be generated.");
        }
        return hash;
    }
    
    /**
     * Save the calculated hash to the specified output file.
     * 
     * @param hash The calculated hash.
     * @param filename The filename in which to save the hash.
     */
    public void saveHash(
                    String hash,
                    Path   output) {

        if (output != null) {
            try (BufferedWriter writer = 
                    Files.newBufferedWriter(output, Charset.forName("UTF-8"))) {
                writer.write(hash);
                writer.flush();
            }
            catch (IOException ioe) {
                LOGGER.error("Unexpected IOException exception "
                        + "encountered while attempting to save the hash "
                        + "String to an output file.  Exception message => [  "
                        + ioe.getMessage()
                        + " ].");
            }
        }
        else {
            LOGGER.error("Path to the output hash file is null.  Generated "
                    + "cannot be saved to an on-disk file.");
        }
    }

}
