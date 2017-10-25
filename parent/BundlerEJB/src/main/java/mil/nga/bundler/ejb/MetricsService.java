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

import mil.nga.bundler.exceptions.ServiceUnavailableException;
import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.model.BundlerMetrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session Bean implementation class MetricsService
 */
@Stateless
@LocalBean
public class MetricsService implements BundlerConstantsI {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
            MetricsService.class);
    
    /**
     * JPA persistence entity manager.
     */
    @PersistenceContext(unitName=APPLICATION_PERSISTENCE_CONTEXT)
    private EntityManager em;
    
    /**
     * Default Eclipse-generated constructor. 
     */
    public MetricsService() { }

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
    

    public BundlerMetrics getMetrics() throws ServiceUnavailableException {
        
        BundlerMetrics metrics = null;
        
        try {
                    
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<BundlerMetrics> cq = cb.createQuery(BundlerMetrics.class);
            Root<BundlerMetrics> root = cq.from(BundlerMetrics.class);
            cq.select(root);
            Query query = getEntityManager().createQuery(cq);
            metrics = (BundlerMetrics)query.getSingleResult();
                
        }
        catch (NoResultException nre) {
                LOGGER.warn("Unable to retrieve BundlerMetrics object from target "
                        + "data store.  javax.persistence.NoResultException "
                     + "encountered.  Error message [ "
                     + nre.getMessage()
                     + " ].");    
        }
        return metrics;
        
    }
    
    
    public BundlerMetrics update(BundlerMetrics metrics) {
        
        BundlerMetrics managedMetrics = null;
        
        if (em != null) {
            if (metrics != null) {
                
                managedMetrics = em.merge(metrics);
                em.flush();
                
            }
            else {
                LOGGER.warn("Called with a null or empty BundlerMetrics "
                        + "object.  Object will not be persisted.");
            }
        }
        else {
            LOGGER.error("The container injected EntityManager object is "
                    + "null.  Unable to persist the BundlerMetrics "
                    + "object from the data store.");
        }
        return managedMetrics;
    }
}
