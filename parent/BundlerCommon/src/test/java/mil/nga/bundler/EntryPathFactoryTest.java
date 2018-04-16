package mil.nga.bundler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import mil.nga.util.URIUtils;

/**
 * jUnit tests for the EntryPathFactory class.
 * 
 * @author L. Craig Carpenter
 */
public class EntryPathFactoryTest {
	
    /**
     * Properties object populated in the init method.
     */
    private static Properties props = new Properties();
    
    /**
     * The initialization method will set up the properties object for use 
     * throughout the test methods.
     */
    @BeforeClass
    public static void init() {
        if (props == null) {
            props = new Properties();
        }
        props.setProperty("bundler.exclude_path_prefix_0", "/mnt/raster");
        props.setProperty("bundler.exclude_path_prefix_1", "/mnt/fbga");
        props.setProperty("bundler.exclude_path_prefix_2", "/mnt/nonstd");
    }
    
    @Test
    public void testLoadPrefixPathExclusions() {
        List<String> exclusions = EntryPathFactory.getInstance(props).getPrefixExclusions();
        assertNotNull(exclusions);
        assertEquals(3, exclusions.size());
        Map<String, String> testMap = new HashMap<String,String>();
        for (String value : exclusions) {
            testMap.put(value, value);
        }
        assertEquals("/mnt/raster", testMap.get("/mnt/raster"));
        assertEquals("/mnt/fbga", testMap.get("/mnt/fbga"));
        assertEquals("/mnt/nonstd", testMap.get("/mnt/nonstd"));
    }
    
    @Test
    public void testGetEntryPath() {
        String fullPath = "/mnt/raster/bogus/fake/faux/sham/file.txt";
        assertEquals("bogus/fake/faux/sham/file.txt", EntryPathFactory
                .getInstance(props)
                .getEntryPath(
                        URIUtils.getInstance().getURI(fullPath)));
        fullPath = "/mnt/fbga/bogus/fake/faux/sham/file.txt";
        assertEquals("bogus/fake/faux/sham/file.txt", EntryPathFactory
                .getInstance(props)
                .getEntryPath(
                        URIUtils.getInstance().getURI(fullPath)));
        fullPath = "/mnt/nonstd/bogus/fake/faux/sham/file.txt";
        assertEquals("bogus/fake/faux/sham/file.txt", EntryPathFactory
                .getInstance(props)
                .getEntryPath(
                        URIUtils.getInstance().getURI(fullPath)));
        fullPath = "s3://s3.amazonaws.com/mnt/raster/bogus/fake/faux/sham/file.txt";
        assertEquals("bogus/fake/faux/sham/file.txt", EntryPathFactory
                .getInstance(props)
                .getEntryPath(
                        URIUtils.getInstance().getURI(fullPath)));
        fullPath = "s3://s3.amazonaws.com/mnt/fbga/bogus/fake/faux/sham/file.txt";
        assertEquals("bogus/fake/faux/sham/file.txt", EntryPathFactory
                .getInstance(props)
                .getEntryPath(
                        URIUtils.getInstance().getURI(fullPath)));
        fullPath = "s3://s3.amazonaws.com/mnt/nonstd/bogus/fake/faux/sham/file.txt";
        assertEquals("bogus/fake/faux/sham/file.txt", EntryPathFactory
                .getInstance(props)
                .getEntryPath(
                        URIUtils.getInstance().getURI(fullPath)));
    }
    
    @Test
    public void testGetEntryPath2() {
        String fullPath    = "/mnt/bogus/fake/faux/sham/file.txt";
        String baseDir     = "/mnt/bogus/fake";
        String archivePath = "/vvod";
        assertEquals("vvod/faux/sham/file.txt", EntryPathFactory
                .getInstance(props)
                .getEntryPath(
                        URIUtils.getInstance().getURI(fullPath),
                        baseDir, 
                        archivePath));
        // Re-run the test with a full s3 URI
        fullPath = "s3://s3.amazonaws.com/mnt/bogus/fake/faux/sham/file.txt";
        assertEquals("vvod/faux/sham/file.txt", EntryPathFactory
                .getInstance(props)
                .getEntryPath(
                        URIUtils.getInstance().getURI(fullPath),
                        baseDir, 
                        archivePath));
    }
    
    @Test
    public void testTruncateFilename() {
        String filename = "0123456789012345678901234567890123456789" 
                + "0123456789012345678901234567890123456789"
                + "01234567890123456789ABCDEFGHIJK.txt";
        assertEquals("012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345.txt",
                EntryPathFactory.getInstance(props).truncateFilename(filename));
    }
    
    @Test
    public void testGetExtension() {
        EntryPathFactory factory = EntryPathFactory.getInstance(props);
        assertEquals(".gz", factory.getExtension("blah.tar.gz"));
        assertEquals("", factory.getExtension("file_with_no_extension"));
        assertEquals(".txt", factory.getExtension("/tmp/dir1/dir2/blah.txt"));
    }

    @Test
    public void testLengthLimitEnforcement() {
        String filename = "/abcd/efgh/ijkl/"
                + "0123456789012345678901234567890123456789" 
                + "0123456789012345678901234567890123456789"
                + "0123456789.txt";
        assertEquals(
                "ijkl/012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789.txt",
                EntryPathFactory.getInstance(props).enforceLengthLimit(filename));
        
    }
}
