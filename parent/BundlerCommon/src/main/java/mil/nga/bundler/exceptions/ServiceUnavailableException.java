package mil.nga.bundler.exceptions;

import java.io.Serializable;

/**
 * Exception thrown if the application is unable to look up a target EJB.
 * 
 * @author L. Craig Carpenter
 */
public class ServiceUnavailableException extends Exception implements Serializable {

    /** 
     * Default constructor requiring a message String.
     * @param msg Information identifying why the exception was raised.
     */
    public ServiceUnavailableException(String msg) {
        super(msg);
    }

}