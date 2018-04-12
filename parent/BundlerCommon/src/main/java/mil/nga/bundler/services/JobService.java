package mil.nga.bundler.services;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.model.Job;
import mil.nga.bundler.exceptions.ServiceUnavailableException;

/**
 * Non-EJB implementation of the JobService class.  This class implements
 * the JPA interface for the <code>JOBS</code> table in the back-end data 
 * store.  
 *   
 * @author L. Craig Carpenter
 */
public class JobService implements BundlerConstantsI, Closeable {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
            JobService.class);
    
    /**
     * EntityManager object used throughout the class.
     */
    private EntityManager em;
    
    /**
     * Default constructor. 
     */
    public JobService() { }

    /**
     * Accessor method for the EntityManager object that will be used to 
     * interact with the backing data store.
     * 
     * @return A constructed EntityManager object.
     * @throws ServiceUnavailableException Thrown if we are unable to 
     * construct the EntityManager. 
     */
    private EntityManager getEntityManager() 
            throws ServiceUnavailableException {
        if (em == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating un-managed EntityManager.");
            }
            EntityManagerFactory emFactory =  
                    Persistence.createEntityManagerFactory(
                            APPLICATION_PERSISTENCE_CONTEXT);
            
            if (emFactory != null) {
                em = emFactory.createEntityManager();
            }
            else {
                LOGGER.warn("The EntityManagerFactory object is null.  Unable "
                        + "to start the JPA subsystem.");
            }
            if (em == null) {
                throw new ServiceUnavailableException(
                        "Unable to start the JPA subsystem.  Unable to "
                        + "construct the EntityManager.");
            }
        }
        return em;
    }
    
    
    /**
     * Retrieve a Job object from the target database.
     * 
     * @param jobID The job ID (primary key) of the job to retrieve.
     * @return The target Job object.  Null if the Job could not be found.
     */
    public Job getJob(String jobID) throws ServiceUnavailableException {
        
        Job job = null;
        
        if ((jobID != null) && (!jobID.isEmpty())) {
            try {
                
                CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
                CriteriaQuery<Job> cq = cb.createQuery(Job.class);
                Root<Job> root = cq.from(Job.class);
                
                // Add the "where" clause
                cq.where(
                        cb.equal(
                                root.get("jobID"), 
                                cb.parameter(String.class, "jobID")));
                
                // Create the query
                Query query = getEntityManager().createQuery(cq);
                
                // Set the value for the where clause
                query.setParameter("jobID", jobID);
                
                // Retrieve the data
                job = (Job)query.getSingleResult();
                
            }
            catch (NoResultException nre) {
                LOGGER.warn("Unable to find Job associated with job ID [ "
                        + jobID
                        + " ].  Returned Job will be null.");
            }
        }
        else {
            LOGGER.warn("The input job ID is null or empty.  Unable to "
                    + "retrieve an associated job.");
        }
        return job;
    }
    
    /**
     * Get a list of all jobIDs currently residing in the target data store.
     * 
     * @return A list of jobIDs
     */
    @SuppressWarnings("unchecked")
    public List<String> getJobIDs() throws ServiceUnavailableException {
        
        List<String> jobIDs = null;
        
        try {
            
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Job> cq =
                            cb.createQuery(Job.class);
            Root<Job> e = cq.from(Job.class);
            cq.select(e.get("jobID"));
            Query query = getEntityManager().createQuery(cq);
            jobIDs = query.getResultList();
            
        }
        catch (NoResultException nre) {
            LOGGER.warn("Unable to find any job IDs in the data store.  "
                    + "Returned list will be empty.");
            jobIDs = new ArrayList<String>();
        }
        return jobIDs;
    }
    
    /**
     * Update the data in the back end database with the current contents 
     * of the Job.
     * 
     * @param job The Job object to update.
     * @return The container managed Job object.
     */
    public Job update(Job job) throws ServiceUnavailableException {
        
        long start      = System.currentTimeMillis();
        Job  managedJob = null;
        
        if (job != null) {
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("...beginning update of job [ "
                        + job.getJobID()
                        + " ]...");
            }
            
            getEntityManager().getTransaction().begin();
            managedJob = getEntityManager().merge(job);
            getEntityManager().getTransaction().commit();
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Job updated in [ "
                        + (System.currentTimeMillis() - start) 
                        + " ] ms.");
            }
        }
        else {
            LOGGER.warn("Called with a null or empty Job object.  "
                    + "Object will not be persisted.");
        }

        return managedJob;
    }
    
    /**
     * Persist the input Job object into the back-end data store.
     * 
     * @param job The Job object to persist.
     */
    public void persist(Job job) throws ServiceUnavailableException {
        
        long start = System.currentTimeMillis();
        
        if (job != null) {
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("...beginning persist of job [ "
                        + job.getJobID()
                        + " ]...");
            }
            
            getEntityManager().getTransaction().begin();
            getEntityManager().persist(job);
            getEntityManager().getTransaction().commit();
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Job persisted in [ "
                        + (System.currentTimeMillis() - start) 
                        + " ] ms.");
            }
            
        }
        else {
            LOGGER.warn("Called with a null or empty Job object.  "
                    + "Object will not be persisted.");
        }
    }
    
    /**
     * Implementation of the <code>close()</code> method required by the 
     * <code>Closeable</code> interface used to close the constructed 
     * <code>EntityManager</code> object. 
     */
    @Override
    public void close() {
        if (em != null) {
            em.close();
        }
    }
    
}
