package mil.nga.bundler.ejb;

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
import mil.nga.bundler.model.ArchiveJob;

/**
 * 
 * This class was introduced to help solve 
 */
@Stateless
@LocalBean
public class ArchiveJobService implements BundlerConstantsI {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
            ArchiveJobService.class);
    
    /**
     * JPA persistence entity manager.
     */
    @PersistenceContext(unitName=APPLICATION_PERSISTENCE_CONTEXT)
    private EntityManager em;
    
    /**
     * Default constructor. 
     */
    public ArchiveJobService() { }
    
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
     * Retrieve an ArchiveJob object from the backing data store.
     * 
     * @param jobID The jobID to retrieve. 
     * @param archiveID The archiveID to retrieve.
     * 
     * @return The ArchiveJob object matching the input jobID and ArchiveID.
     * May return null.
     */
    public ArchiveJob getArchiveJob(String jobID, long archiveID) 
            throws ServiceUnavailableException {
        
        long      startTime = System.currentTimeMillis();
        ArchiveJob archive   = null;

        if ((jobID != null) && (!jobID.isEmpty())) {
            try {
                
                CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
                CriteriaQuery<ArchiveJob> cq = cb.createQuery(ArchiveJob.class);
                Root<ArchiveJob> root = cq.from(ArchiveJob.class);
                
                // Add the "where" clause
                cq.where(
                        cb.equal(
                                root.get("jobID"), 
                                cb.parameter(String.class, "jobID")),
                        cb.equal(root.get("archiveID"), 
                                cb.parameter(Long.class, "archiveID")));
                
                // Create the query
                Query query = getEntityManager().createQuery(cq);
                
                // Set the values for the where clause
                query.setParameter("jobID", jobID);
                query.setParameter("archiveID", archiveID);
                
                // Retrieve the data
                archive = (ArchiveJob)query.getSingleResult();
                
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Target ArchiveJob record for job ID [ "
                            + jobID
                            + " ] and archive ID [ "
                            + archiveID
                            + " ] retrieved in [ "
                            + (System.currentTimeMillis() - startTime)
                            + " ] ms.");
                }
            }
            catch (NoResultException nre) {
                LOGGER.info("javax.persistence.NoResultException "
                        + "encountered.  Error message [ "
                        + nre.getMessage()
                        + " ].  Returned Archive Job will be null.");
            }
        }
        else {
            LOGGER.warn("The input job ID is null or empty.  "
                    + "The ArchiveJob will not be updated.");
        }
        return archive;
    }
    
    /**
     * Method used to update the input ArchiveJob object in the backing data 
     * store.
     * 
     * @param archive ArchiveJob object to update.
     */
    public void update(ArchiveJob archive) throws ServiceUnavailableException {
        
        long startTime = System.currentTimeMillis();
        
        if (archive != null) {
                
            //getEntityManager().getTransaction().begin();
            getEntityManager().merge(archive);
            getEntityManager().flush();
            //getEntityManager().getTransaction().commit();
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("ArchiveJob object updated in [ "
                        + (System.currentTimeMillis() - startTime)
                        + " ] ms.");
            }
        }
        else {
            LOGGER.warn("The input ArchiveJob object is null or empty.  "
                    + "The ArchiveJob will not be updated.");
        }
    }
}
