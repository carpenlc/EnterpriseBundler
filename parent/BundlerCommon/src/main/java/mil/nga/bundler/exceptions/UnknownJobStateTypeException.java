package mil.nga.bundler.exceptions;

public class UnknownJobStateTypeException extends Exception {

    /**
     * Eclipse-generated serialVersionUID
     */
    private static final long serialVersionUID = 6907464659597347041L;

    /** 
     * Default constructor requiring a message String.
     * @param msg Information identifying why the exception was raised.
     */
    public UnknownJobStateTypeException(String msg) {
        super(msg);
    }
    
}
