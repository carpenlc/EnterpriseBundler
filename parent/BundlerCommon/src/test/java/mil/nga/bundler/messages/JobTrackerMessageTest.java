package mil.nga.bundler.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import mil.nga.bundler.types.JobStateType;

public class JobTrackerMessageTest {

    private final String TEST_JOB_ID    = "F79A57E5A250E33681EDF0C36A74";
    private final String TEST_USER_NAME = "MARLEY.ROBERT.N.1234567890";
    
    // Represents a simple, valid deserializable JSON String.
    private static String deserializableJSON;
    
    @BeforeClass
    public static void initialize() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"threads\":0,\"threads_complete\":0,\"elapsed_time\":0,");
        sb.append("\"hashes_complete\":0,\"num_files\":0,\"files_complete\":0,");
        sb.append("\"size\":0,\"size_complete\":0,\"archives\":[],");
        sb.append("\"job_id\":\"F79A57E5A250E33681EDF0C36A74\",\"state\":\"NOT_STARTED\",");
        sb.append("\"user_name\":\"unavailable\"");
        sb.append("}");
        deserializableJSON = sb.toString();
    }
    
    @Test
    public void testCreation() {
        
        System.out.println("[TEST] Testing creation of objects of type class "
                + "mil.nga.bundler.message.JobTrackerMessage...");
        
        // Test a simple initialization.
        JobTrackerMessage message = new JobTrackerMessage.JobTrackerMessageBuilder()
                                        .jobID(TEST_JOB_ID)
                                        .userName(TEST_USER_NAME)
                                        .build();
        assertEquals(message.getNumArchives(), 0);
        assertEquals(message.getNumArchivesComplete(), 0);
        assertEquals(message.getNumFiles(), 0);
        assertEquals(message.getNumFilesComplete(), 0);
        assertEquals(message.getNumHashesComplete(), 0);
        assertEquals(message.getSizeComplete(), 0);
        assertEquals(message.getState(), JobStateType.NOT_AVAILABLE);
    }
    

    @Test
    public void testSerialization() {
        System.out.println("[TEST] Testing serialization of class "
                + "mil.nga.bundler.message.JobTrackerMessage...");
        JobTrackerMessage message = new JobTrackerMessage.JobTrackerMessageBuilder()
                .jobID(TEST_JOB_ID)
                .userName(TEST_USER_NAME)
                .build();
        
        String json = BundlerMessageSerializer.getInstance().serialize(message);
        assertNotNull(json);
    }
    
    /**
     * Deserialization does not happen in the production environment.
     * 
     * Note: We can only test de-serialization of JobTrackerMessages that do 
     * not contain completed ArchiveJob objects.  This is because the 
     * serialization of JobTrackerMessages does not output the full 
     * ArchiveJob object.
     */
    @Test
    public void testDeSerialization() {
        System.out.println("[TEST] Testing de-serialization of class "
                + "mil.nga.bundler.message.JobTrackerMessage...");
    
        JobTrackerMessage request = BundlerMessageSerializer.getInstance()
                .deserializeToJobTrackerMessage(deserializableJSON);
        
        assertEquals(request.getJobID(), "F79A57E5A250E33681EDF0C36A74");
        assertEquals(request.getUserName(), "unavailable");
        assertEquals(request.getNumArchives(), 0);
        assertEquals(request.getNumArchivesComplete(), 0);
        assertEquals(request.getNumFiles(), 0);
        assertEquals(request.getNumFilesComplete(), 0);
        assertEquals(request.getNumHashesComplete(), 0);
        assertEquals(request.getSizeComplete(), 0);
        assertEquals(request.getState(), JobStateType.NOT_STARTED);
        
    }

}
