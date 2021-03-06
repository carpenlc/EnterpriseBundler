package mil.nga.bundler.messages;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import mil.nga.bundler.BundleRequest;

/**
 * This class was created for testing purposes.  It will serialize/deserialize
 * message objects to/from it's String-based JSON equivalent.
 * 
 * @author L. Craig Carpenter
 */
public class BundlerMessageSerializer {

    /**
     * Set up the LogBack system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
            BundlerMessageSerializer.class);
    
    /** 
     * DateFormat object used when serializing/deserializing dates.  This 
     * overrides the default behavior which depends on the type of date being
     * serialized/deserialized.
     */
    private static final DateFormat dateFormatter = 
            new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    
    /**
     * Ensure that all times are in GMT
     */
    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    /**
     * Accessor method for the singleton instance of the 
     * JSONSerializer class.
     * 
     * @return The singleton instance of the JSONSerializer .
     * class.
     */
    public static BundlerMessageSerializer getInstance() {
        return BundlerMessageSerializerHolder.getSingleton();
    }    
    
    /**
     * Method used to deserialize a JSON String into an object of type 
     * <code>mil.nga.bundler.BundleRequest</code>
     * 
     * @param json The String in JSON format.
     * @return A <code>mil.nga.bundler.BundleRequest</code> object. 
     * Null if any exceptions were encountered while deserializing the String.
     */
    public BundleRequest deserializeToBundleRequest(String json) {
        
        BundleRequest deserialized = null;
        
        try {
            if (json != null) {
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.setDateFormat(dateFormatter);
                deserialized = mapper.readValue(
                        json, 
                        BundleRequest.class);
                
            }
        }
        catch (JsonMappingException jme) {
            LOGGER.error("Unexpected JsonMappingException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to an object of type BundleRequest.  "
                    + "Exception message => [ "
                    + jme.getMessage()
                    + " ].");
        }
        catch (JsonParseException jpe) {
            LOGGER.error("Unexpected JsonParseException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to an object of type BundleRequest.  "
                    + "Exception message => [ "
                    + jpe.getMessage()
                    + " ].");
        }
        catch (IOException ioe) {
            LOGGER.error("Unexpected IOException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to an object of type BundleRequest.  "
                    + "Exception message => [ "
                    + ioe.getMessage()
                    + " ].");
        }
        return deserialized;
    }
    
    /**
     * Method used to deserialize a JSON String into an object of type 
     * <code>mil.nga.bundler.message.BundleRequestMessage</code>
     * 
     * @param json The String in JSON format.
     * @return A <code>mil.nga.bundler.message.BundleRequestMessage</code> object. 
     * Null if any exceptions were encountered while deserializing the String.
     */
    public BundleRequestMessage deserializeToBundleRequestMessage(String json) {
        
        BundleRequestMessage deserialized = null;
        
        try {
            if (json != null) {
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.setDateFormat(dateFormatter);
                deserialized = mapper.readValue(
                        json, 
                        BundleRequestMessage.class);
                
            }
        }
        catch (JsonMappingException jme) {
            LOGGER.error("Unexpected JsonMappingException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to an object of type BundleRequestMessage.  "
                    + "Exception message => [ "
                    + jme.getMessage()
                    + " ].");
        }
        catch (JsonParseException jpe) {
            LOGGER.error("Unexpected JsonParseException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to an object of type BundleRequestMessage.  "
                    + "Exception message => [ "
                    + jpe.getMessage()
                    + " ].");
        }
        catch (IOException ioe) {
            LOGGER.error("Unexpected IOException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to an object of type BundleRequestMessage.  "
                    + "Exception message => [ "
                    + ioe.getMessage()
                    + " ].");
        }
        return deserialized;
    }
    
    /**
     * Method used to deserialize a JSON String into an object of type 
     * <code>mil.nga.bundler.message.JobTrackerMessage</code>
     * 
     * @param json The String in JSON format.
     * @return A <code>mil.nga.bundler.message.JobTrackerMessage</code> object. 
     * Null if any exceptions were encountered while deserializing the String.
     */
    public JobTrackerMessage deserializeToJobTrackerMessage(String json) {
        
        JobTrackerMessage deserialized = null;
        
        try {
            if (json != null) {
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.setDateFormat(dateFormatter);
                deserialized = mapper.readValue(
                        json, 
                        JobTrackerMessage.class);
                
            }
        }
        catch (JsonMappingException jme) {
            LOGGER.error("Unexpected JsonMappingException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to an object of type JobTrackerMessage.  "
                    + "Exception message => [ "
                    + jme.getMessage()
                    + " ].");
        }
        catch (JsonParseException jpe) {
            LOGGER.error("Unexpected JsonParseException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to an object of type JobTrackerMessage.  "
                    + "Exception message => [ "
                    + jpe.getMessage()
                    + " ].");
        }
        catch (IOException ioe) {
            LOGGER.error("Unexpected IOException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to an object of type JobTrackerMessage.  "
                    + "Exception message [ "
                    + ioe.getMessage()
                    + " ].");
        }
        return deserialized;
    }
    
    /**
     * Method used to deserialize a JSON String into an object of type 
     * <code>mil.nga.bundler.message.FileRequest</code>
     * 
     * @param json The String in JSON format.
     * @return A <code>mil.nga.bundler.message.FileRequest</code> object. 
     * Null if any exceptions were encountered while deserializing the String.
     */
    public FileRequest deserializeToFileRequest(String json) {
        
        FileRequest deserialized = null;
        
        try {
            if (json != null) {
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.setDateFormat(dateFormatter);
                deserialized = mapper.readValue(
                        json, 
                        FileRequest.class);
                
            }
        }
        catch (JsonMappingException jme) {
            LOGGER.error("Unexpected JsonMappingException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to an object of type FileRequest.  Exception "
                    + "message [ "
                    + jme.getMessage()
                    + " ].");
        }
        catch (JsonParseException jpe) {
            LOGGER.error("Unexpected JsonParseException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to an object of type FileRequest.  Exception "
                    + "message [ "
                    + jpe.getMessage()
                    + " ].");
        }
        catch (IOException ioe) {
            LOGGER.error("Unexpected IOException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to an object of type FileRequest.  Exception "
                    + "message [ "
                    + ioe.getMessage()
                    + " ].");
        }
        return deserialized;
    }
    
    /**
     * Method used to deserialize a JSON array into a List of String objects.
     * 
     * @param json The String in JSON format.
     * @return A <code>java.util.List</code> object containing String objects.
     * Null if any exceptions were encountered while deserializing the String.
     */
    public List<String> deserializeToStringList(String json) {
        
        List<String> deserialized = null;
        
        try {
            if (json != null) {
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.setDateFormat(dateFormatter);
                CollectionType outputType = mapper.getTypeFactory()
                        .constructCollectionType(List.class, String.class);
                deserialized = mapper.readValue(json, outputType);
                
            }
        }
        catch (JsonMappingException jme) {
            LOGGER.error("Unexpected JsonMappingException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON to a List of String objects.  Exception "
                    + "message [ "
                    + jme.getMessage()
                    + " ].");
        }
        catch (JsonParseException jpe) {
            LOGGER.error("Unexpected JsonParseException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON into a List of String objects.  Exception "
                    + "message [ "
                    + jpe.getMessage()
                    + " ].");
        }
        catch (IOException ioe) {
            LOGGER.error("Unexpected IOException encountered "
                    + "while attempting to deserialize the input "
                    + "JSON into a List of String objects.  Exception "
                    + "message [ "
                    + ioe.getMessage()
                    + " ].");
        }
        return deserialized;
    }
    /**
     * Convert the input object into JSON format.  This version of the 
     * serialization process is meant for generating a more human-readable
     * output.
     * 
     * @param obj A populated object.
     * @return A JSON String representation of the input Object.
     */
    public String serializePretty(Object obj) {
        
        String json = "null";
        
        if (obj != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setDateFormat(dateFormatter);
                json = mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(obj);
            }
            catch (JsonProcessingException jpe) {
                LOGGER.error("Unexpected JsonProcessingException encountered "
                        + "while attempting to marshall the input "
                        + "object to JSON.  Exception message [ "
                        + jpe.getMessage()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("Input object is null.  Unable to "
                    + "marshall the object to JSON.");
        }
        return json;
    }
    
    /**
     * Convert the input object into JSON format. 
     * 
     * @param obj A populated object.
     * @return A JSON String representation of the input Object.
     */
    public String serialize(Object obj) {
        
        String json = "null";
        
        if (obj != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setDateFormat(dateFormatter);
                json = mapper.writeValueAsString(obj);
            }
            catch (JsonProcessingException jpe) {
                LOGGER.error("Unexpected JsonProcessingException encountered "
                        + "while attempting to marshall the input "
                        + "object to JSON.  Exception message [ "
                        + jpe.getMessage()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("Input object is null.  Unable to "
                    + "marshall the object to JSON.");
        }
        return json;
    }
    
    /**
     * Static inner class used to construct the Singleton object.  This class
     * exploits the fact that classes are not loaded until they are referenced
     * therefore enforcing thread safety without the performance hit imposed
     * by the <code>synchronized</code> keyword.
     * 
     * @author L. Craig Carpenter
     */
    public static class BundlerMessageSerializerHolder {
        
        /**
         * Reference to the Singleton instance of the BundlerMessageSerializer.
         */
        private static BundlerMessageSerializer _instance = 
                new BundlerMessageSerializer();
    
        /**
         * Accessor method for the singleton instance of the 
         * BundlerMessageSerializer.
         * @return The Singleton instance of the BundlerMessageSerializer.
         */
        public static BundlerMessageSerializer getSingleton() {
            return _instance;
        }
        
    }
}
