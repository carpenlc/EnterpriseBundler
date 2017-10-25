package mil.nga.bundler.ejb;

import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.bundler.exceptions.ServiceUnavailableException;
import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.model.FileEntry;
import mil.nga.bundler.types.JobStateType;

/**
 * Session Bean implementation class FileEntryService
 */
@Stateless
@LocalBean
public class FileEntryService implements BundlerConstantsI {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		FileEntryService.class);
    
    /**
     * JPA persistence entity manager.
     */
    @PersistenceContext(unitName=APPLICATION_PERSISTENCE_CONTEXT)
    private EntityManager em;
    
    /**
     * Default Eclipse-generated constructor. 
     */
    public FileEntryService() { }
    
    /**
     * Accessor method for the EntityManager object that will be used to 
     * interact with the backing data store.
     * 
     * @return A constructed EntityManager object.
     */
    private EntityManager getEntityManager() 
    		throws ServiceUnavailableException {
    	if (em == null) {
    		if (LOGGER.isDebugEnabled()) {
    			LOGGER.debug("Container-injected EntityManager is null.  "
    					+ "Creating un-managed EntityManager.");
    		}
    		EntityManagerFactory emFactory = 
    				Persistence.createEntityManagerFactory(
    						APPLICATION_PERSISTENCE_CONTEXT);
    		if (emFactory != null) {
    			em = emFactory.createEntityManager();
    		}
    		else {
    			LOGGER.warn("Unable to create un-managed EntityManager object.");
    		}
    		if (em == null) {
    			throw new ServiceUnavailableException(
        				"Unable to start the JPA subsystem.  The injected "
        				+ "EntityManager object is null.");
    		}
    	}
    	return em;
    }
    
    /**
     * Method used to retrieve one <code>FileEntry</code> object from the 
     * target datasource.  This method assumes that the combination of job 
     * ID, archive ID, and URI will uniquely identify a single 
     * <code>FileEntry</code> record. 
     * 
     * @param jobID The target job ID.
     * @param archiveID The target archive ID.
     * @param uri The file path as a URI.
     * 
     * @return The associated <code>FileEntry</code> object.
     */
    public FileEntry getFileEntry(
    		String jobID, 
    		long archiveID, 
    		String uri) throws ServiceUnavailableException {
    	
    	long      startTime = System.currentTimeMillis();
    	FileEntry fileEntry  = null;
        
        if ((jobID != null) && (!jobID.isEmpty())) {
        	if ((uri != null) && (!uri.isEmpty())) { 
        		try {
	                CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	                CriteriaQuery<FileEntry> cq = cb.createQuery(FileEntry.class);
	                Root<FileEntry> root = cq.from(FileEntry.class);
	                
	                // Add the "where" clause
	                cq.where(
	                        cb.equal(
	                                root.get("jobID"), 
	                                cb.parameter(String.class, "jobID")),
	                        cb.equal(root.get("archiveID"), 
	                        		cb.parameter(Long.class, "archiveID")),
	                        cb.equal(root.get("path"), 
	                        		cb.parameter(String.class, "path")));
	                
	                // Create the query
	                Query query = getEntityManager().createQuery(cq);
	                
	                // Set the values for the where clause
	                query.setParameter("jobID", jobID);
	                query.setParameter("archiveID", archiveID);
	                query.setParameter("path", uri);
	                
	                // Retrieve the data
	                fileEntry = (FileEntry)query.getSingleResult();
	                
	                if (LOGGER.isDebugEnabled()) {
	                	LOGGER.debug("Target FileEntry record => [ "
	                			+ fileEntry.toString()
	                			+ " ] retrieved in [ "
	                			+ (System.currentTimeMillis() - startTime)
	                			+ " ] ms.");
	                }
        		}
                catch (NoResultException nre) {
                    LOGGER.info("javax.persistence.NoResultException "
                            + "encountered.  Error message [ "
                            + nre.getMessage()
                            + " ].  Returned FileEntry object will be null.");
                }
        	}
        	else {
                LOGGER.warn("The input URI is null or empty.  Unable to "
                        + "retrieve an associated FileEntry object.The "
                        + "returned FileEntry object will be null.");
        	}
        }
        else {
            LOGGER.warn("The input job ID is null or empty.  Unable to "
                    + "retrieve an associated FileEntry object.  The "
            		+ "returned FileEntry object will be null.");
        }
        return fileEntry;
    }

    /**
     * Method used to update the JobState of the FileEntry record associated 
     * with the input parameters.  This method assumes that the combination 
     * of job ID, archive ID, and URI will uniquely identify a single 
     * <code>FileEntry</code> record.
     * 
     * @param jobID The target job ID.
     * @param archiveID The target archive ID.
     * @param uri The file path as a URI.
     * @param state The new job state.
     */
    @Asynchronous
    public void updateState (
    		String jobID, 
    		long archiveID, 
			String uri,
	 		JobStateType state) throws ServiceUnavailableException {
    	
    	long startTime = System.currentTimeMillis();
    	
        if ((jobID != null) && (!jobID.isEmpty())) {
        	if ((uri != null) && (!uri.isEmpty())) {
        		if (state != null) {
        			
        			FileEntry entry = getFileEntry(jobID, archiveID, uri);
        			if (entry != null) {
        				
        				entry.setFileState(state);
        				
        				getEntityManager().merge(entry);
        				getEntityManager().flush();
                        
                        if (LOGGER.isDebugEnabled()) {
    	                	LOGGER.debug("FileEntry state updated in [ "
    	                			+ (System.currentTimeMillis() - startTime)
    	                			+ " ] ms.");
        				}
        			}
        			else {
        				LOGGER.error("Unable to find FileEntry object "
        						+ "for job ID [ "
        						+ jobID
                        		+ " ], archive ID [ "
                        		+ archiveID
                        		+ " ], and URI [ "
                        		+ uri
                        		+ " ].  State cannot be updated.");
        			}
        		}
        		else {
                    LOGGER.warn("The input FileEntry state is null.  "
                    		+ "Unable to update state for job ID [ "
                    		+ jobID
                    		+ " ], archive ID [ "
                    		+ archiveID
                    		+ " ], and URI [ "
                    		+ uri
                    		+ " ].");
        		}
        	}
        	else {
                LOGGER.warn("The input URI is null or empty.  Unable to "
                        + "update state for job ID [ "
                        + jobID
                		+ " ] and archive ID [ "
                		+ archiveID
                		+ " ].");
        	}
        }
        else {
            LOGGER.warn("The input job ID is null or empty.  "
                    + "FileEntry state will not be updated.");
        }
    }
}
