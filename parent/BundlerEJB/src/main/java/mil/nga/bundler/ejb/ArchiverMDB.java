package mil.nga.bundler.ejb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.bundler.model.FileEntry;
import mil.nga.bundler.archive.ArchiveFactory;
import mil.nga.bundler.exceptions.ArchiveException;
import mil.nga.bundler.exceptions.ServiceUnavailableException;
import mil.nga.bundler.exceptions.UnknownArchiveTypeException;
import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.interfaces.BundlerI;
import mil.nga.bundler.messages.ArchiveMessage;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.bundler.model.ArchiveJob;
import mil.nga.bundler.types.JobStateType;
import mil.nga.util.FileUtils;
import mil.nga.util.URIUtils;

/**
 * Message-Driven Bean implementation class for: ArchiverMDB
 * 
 * This class receives messages of type 
 * <code>mil.nga.bundler.messages.ArchiveMessage</code> from the cluster JMS 
 * queue defined by the class annotations.  It then runs the bundle process 
 * for the ArchiveJob identified by the Job ID/Archive ID combination that was
 * specified in the incoming <code>mil.nga.bundler.messages.ArchiveMessage</code> 
 * object.
 * 
 * Important note:  Make sure that if this class is deployed to a test server 
 * that is even on the same network as a production cluster you need to make 
 * sure the queue names are different.  If they are the same the test cluster 
 * they can read messages from the queue and attempt to process them.
 * 
 * The following files need to be modified to change the queue name from 
 * production to test and vice versa:
 * 
 * <code>mil.nga.bundler.ejb.ArchiverMDB</code>
 * <code>mil.nga.bundler.ejb.JobTrackerMDB</code>
 * 
 * @author L. Craig Carpenter 
 */
@MessageDriven(
                // Note to self, if your MDB implements any interfaces other 
                // than MessageListener, you have to specify which one is the 
                // MessageListener.
                messageListenerInterface=MessageListener.class,
                name = "ArchiverMDB",
                activationConfig = {
                                @ActivationConfigProperty(
                                                propertyName = "destinationType",
                                                propertyValue = "javax.jms.Queue"),
                                // Another note to self, even though we moved the 
                                // EJB definitions to ejb-jar.xml you still have to 
                                // have the destination annotation or the project will
                                // not deploy.
                                @ActivationConfigProperty(
                                                propertyName = "destination",
                                                propertyValue = "queue/ArchiverMessageQ_TEST"),
                                @ActivationConfigProperty(
                                                propertyName = "acknowledgeMode",
                                                propertyValue = "Auto-acknowledge")
                })
public class ArchiverMDB implements MessageListener, BundlerConstantsI {

    /**
     * Set up the Log4j system for use throughout the class
     */
    static final Logger LOGGER = LoggerFactory.getLogger(ArchiverMDB.class);
    
    /**
     * Container-injected reference to the JobService EJB.
     */
    @EJB
    BundlerService bundlerService;
    
    /**
     * Default constructor. 
     */
    public ArchiverMDB() { }
    
    /**
     * Private method used to obtain a reference to the target EJB.  
     * 
     * Method implemented because JBoss EAP 6.x was inexplicably NOT always
     * injecting the EJB (i.e. EJB reference was null)
     * 
     * @return Reference to the BundlerService EJB.
     * @throws ServiceUnavailableException Thrown if we are unable to obtain
     * a reference to the target EJB.
     */
    private BundlerService getBundlerService() 
    		throws ServiceUnavailableException {
        
    	if (bundlerService == null) {
            LOGGER.warn("Application container failed to inject the "
                    + "reference to BundlerService.  Attempting to "
                    + "look it up via JNDI.");
            bundlerService = EJBClientUtilities
                    .getInstance()
                    .getBundlerService();
            if (bundlerService == null) {
                throw new ServiceUnavailableException("Unable to obtain a "
                		+ "reference to [ "
                        + BundlerService.class.getCanonicalName()
                        + " ].");
            }
        }
        return bundlerService;
    }
    
    /**
     * This method invokes the bundler processing for a single archive job.
     * It listens for messages placed on the JMS Queue queue/ArchiverMessageQ.
     * When a message is received it unwraps the Archive object from the 
     * JMS ObjectMessage and then invokes an asynchronous method to perform 
     * the bundle processing.
     * 
     * @see MessageListener#onMessage(Message)
     */
    @Override
    public void onMessage(Message message) {
        
    	long startTime = System.currentTimeMillis();
    	
        try {
            
            ObjectMessage objMessage = (ObjectMessage)message;
            ArchiveMessage archiveMsg = (ArchiveMessage)objMessage.getObject();
            
            if (archiveMsg != null) {
	            
            	LOGGER.info("ArchiverMDB received notification to process [ " 
	                    + archiveMsg.toString()
	                    + " ].");
	            
	            getBundlerService().handleMessage(archiveMsg);
	            
	            // TODO: test code.  remove.
	            LOGGER.info("Asynchronous call completed in [ "
	            		+ (System.currentTimeMillis() - startTime) 
	            		+ " ] ms.");
            }
            else {
            	LOGGER.error("Internal system failure.  Unable to unpack the "
        			+ "incoming JMS message.");
            }
            
        }
        catch (JMSException jmsEx) {
            LOGGER.error("Unexpected JMSException encountered while attempting "
                    + "to retrieve the ArchiveMessage from the target message "
                    + "queue.  Error message => [ "
                    + jmsEx.getMessage()
                    + " ].");
        }
        catch (ServiceUnavailableException sue) {
        	LOGGER.error("Internal system failure.  Target EJB service "
        			+ "is unavailable.  Exception message => [ "
        			+ sue.getMessage()
        			+ " ].");
        }
    }
    


}
