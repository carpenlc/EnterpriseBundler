package mil.nga.bundler.types;

import mil.nga.bundler.exceptions.UnknownJobStateTypeException;

/**
 * Enumeration type identifying the status of a Bundler job.
 *  
 * @author L. Craig Carpenter
 */
public enum JobStateType {
    NOT_STARTED("not_started"),
    IN_PROGRESS("in_progress"),
    INVALID_REQUEST("invalid_request"),
    COMPRESSING("compressing"),
    CREATING_HASH("creating_hash"),
    COMPLETE("complete"),
    ERROR("error");
    
    /**
     * The text field.
     */
    private final String text;
    
    /**
     * Default constructor
     * @param text Text associated with the enumeration value.
     */
    private JobStateType(String text) {
        this.text = text;
    }
    
    /**
     * Getter method for the text associated with the enumeration value.
     * 
     * @return The text associated with the instanced enumeration type.
     */
    public String getText() {
        return this.text;
    }
    
    /**
     * Convert an input String to it's associated enumeration type.  There
     * is no default type, if an unknown value is supplied an exception is
     * raised.
     * 
     * @param text Input text information
     * @return The appropriate ArchiveType enum value.
     * @throws UnknownJobStateTypeException Thrown if the caller submitted a String 
     * that did not match one of the existing ArchiveTypes. 
     */
    public static JobStateType fromString(String text) 
            throws UnknownJobStateTypeException {
        if (text != null) {
            for (JobStateType type : JobStateType.values()) {
                if (text.trim().equalsIgnoreCase(type.getText())) {
                    return type;
                }
            }
        }
        throw new UnknownJobStateTypeException("Unknown job state type requested!  " 
                + "Job State Type requested [ " 
                + text
                + " ].");
    }
}
