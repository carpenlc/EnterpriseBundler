package mil.nga.bundler.ejb;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.bundler.exceptions.ServiceUnavailableException;
import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.model.Job;
import mil.nga.bundler.types.JobStateType;

/**
 * Session Bean implementation class JobService
 * 
 */
@Stateless
@LocalBean
public class JobService implements BundlerConstantsI {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
            JobService.class);
    
    /**
     * JPA persistence entity manager.
     */
    @PersistenceContext(unitName=APPLICATION_PERSISTENCE_CONTEXT)
    private EntityManager em;
    
    /**
     * Default Eclipse-generated constructor. 
     */
    public JobService() { }

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
     * Get a list of Jobs that have not yet completed.
     * 
     * @return A list of jobs in a state other than "COMPLETE".
     */
    public List<Job> getIncompleteJobs() throws ServiceUnavailableException {
        
    	long      start = System.currentTimeMillis();
    	List<Job> jobs  = null;
        
        try {
        	
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Job> cq =
                            cb.createQuery(Job.class);
            Root<Job> rootEntry = cq.from(Job.class);
            CriteriaQuery<Job> all = cq.select(rootEntry);
            cq.where(cb.notEqual(rootEntry.get("state"), JobStateType.COMPLETE));
            cq.orderBy(cb.desc(rootEntry.get("startTime")));
            TypedQuery<Job> allQuery = getEntityManager().createQuery(all);
            jobs = allQuery.getResultList();
            
        }
        catch (NoResultException nre) {
            LOGGER.info("javax.persistence.NoResultException "
                    + "encountered.  Error message [ "
                    + nre.getMessage()
                    + " ].  Returned List<Job> object will be null.");
            jobs = new ArrayList<Job>();
        }
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Incomplete job list retrieved in [ "
                    + (System.currentTimeMillis() - start) 
                    + " ] ms.");
        }
        
        return jobs;
    }
    
    /**
     * Retrieve a Job object from the target database.
     * 
     * @param jobID The job ID (primary key) of the job to retrieve.
     * @return The target Job object.  Null if the Job could not be found.
     */
    public Job getJob(String jobID) throws ServiceUnavailableException {
    	
    	long start = System.currentTimeMillis();
        Job  job   = null;
        
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

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Job [ "
            		+ jobID
            		+ " ] retrieved in [ "
                    + (System.currentTimeMillis() - start) 
                    + " ] ms.");
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
    	
    	long         start  = System.currentTimeMillis();
        List<String> jobIDs = null;
        
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Job> cq =
                            cb.createQuery(Job.class);
            Root<Job> e = cq.from(Job.class);
            cq.multiselect(e.get("jobID"));
            Query query = getEntityManager().createQuery(cq);
            jobIDs = query.getResultList();
	    }
	    catch (NoResultException nre) {
	    	LOGGER.warn("Unable to find any job IDs in the data store.  "
	    			+ "Returned list will be empty.");
	    	jobIDs = new ArrayList<String>();
	    }
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Job IDs retrieved in [ "
                    + (System.currentTimeMillis() - start) 
                    + " ] ms.");
        }
        return jobIDs;
    }
    
    /**
     * Return a list of all Job objects in the target data store.
     * @return All existing Job objects.
     */
    public List<Job> getJobs() throws ServiceUnavailableException {
        
    	long      start = System.currentTimeMillis();
        List<Job> jobs  = null;
        
        try {
        	
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Job> cq = cb.createQuery(Job.class);
            Root<Job> root = cq.from(Job.class);
            
            // Add the "order by" clause sorting by time
            cq.orderBy(cb.desc(root.get("startTime"))); 
            
            // Create the query
            TypedQuery<Job> query = getEntityManager().createQuery(cq);
            
            // Retrieve the data
            jobs = query.getResultList();
            
        }
	    catch (NoResultException nre) {
	         LOGGER.warn("javax.persistence.NoResultException "
	                 + "encountered.  Error message [ "
	                 + nre.getMessage()
	                 + " ].");  
	         jobs = new ArrayList<Job>();
	    }
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Job list retrieved in [ "
                    + (System.currentTimeMillis() - start) 
                    + " ] ms.");
        }
        
        return jobs;
    }
    
    /**
     * This method will retrieve Job objects from the database that 
     * have a start_time (startTime) that fall between the input startTime and
     * endTime parameters.  The time data stored in the database are not dates, 
     * but long values.  As such, the two time parameters should be formatted as 
     * long time values (i.e. milliseconds from epoch).  
     * 
     * @param startTime Earliest time in the time slice to query.
     * @param endTime Latest time in the time slice to query.
     * @return A list of jobs in with a start time that fall between the two 
     * input dates.
     */

    public List<Job> getJobsByDate(
    		long startTime, 
    		long endTime) throws ServiceUnavailableException {
    	
    	long      start = System.currentTimeMillis();
        List<Job> jobs  = null;
        
        // Ensure the startTime is earlier than the endTime before submitting
        // the query to the database.
        if (startTime > endTime) {
                LOGGER.warn("The caller supplied a start time that falls "
                        + "after the end time.  Swapping start and end "
                        + "times.");
                long temp = startTime;
                startTime = endTime;
                endTime = temp;
        }
        else if (startTime == endTime) {
            LOGGER.warn("The caller supplied the same time for both start "
                    + "and end time.  This method will likely yield a null "
                    + "job list.");
        }
        
        try {
        	
             CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
             CriteriaQuery<Job> cq =
                             cb.createQuery(Job.class);
             Root<Job> rootEntry = cq.from(Job.class);
             CriteriaQuery<Job> all = cq.select(rootEntry);

             Path<Long> pathToStartTime = rootEntry.get("startTime");
             cq.where(cb.between(pathToStartTime, startTime, endTime));

             cq.orderBy(cb.desc(pathToStartTime));
             TypedQuery<Job> allQuery = getEntityManager().createQuery(all);
             jobs = allQuery.getResultList();     
             
        }
        catch (NoResultException nre) {
             LOGGER.warn("javax.persistence.NoResultException "
                     + "encountered.  Error message [ "
                     + nre.getMessage()
                     + " ].");    
        }
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Job list retrieved in [ "
                    + (System.currentTimeMillis() - start) 
                    + " ] ms.");
        }
        return jobs;
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
        	// getEntityManager().getTransaction().begin();
            managedJob = getEntityManager().merge(job);
            getEntityManager().flush();
            // getEntityManager().getTransaction().commit();
        }
        else {
            LOGGER.warn("Called with a null or empty Job object.  "
                    + "Object will not be persisted.");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Job updated in [ "
                    + (System.currentTimeMillis() - start) 
                    + " ] ms.");
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
        	// getEntityManager().getTransaction().begin();
        	getEntityManager().persist(job);
        	getEntityManager().flush();
            // getEntityManager().getTransaction().commit();
        }
        else {
            LOGGER.warn("Called with a null or empty Job object.  "
                    + "Object will not be persisted.");
        }
    	
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Job persisted in [ "
                    + (System.currentTimeMillis() - start) 
                    + " ] ms.");
        }
    }
}
