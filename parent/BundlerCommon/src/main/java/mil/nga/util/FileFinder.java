package mil.nga.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * This class implements logic that works much like the UNIX "find" command.
 * Clients must supply a starting path location and a pattern to match.  
 * This class will then walk through the file tree looking for files that
 * match the input pattern.
 * 
 * Note: This class will only work in Java 1.7 or above.
 * 
 * @author L. Craig Carpenter
 */
public class FileFinder {

    /**
     * Execute a search on the filesystem for files that match the input 
     * pattern.
     * 
     * @param path The starting location for the search.
     * @param pattern The file pattern to look for.
     * @exception IOException Thrown during the search process.
     */
    public static List<Path> find(URI uri, String pattern) 
            throws IOException {
        
        Path start = null;

        start = Paths.get(uri);
        
        Finder finder = new Finder(uri, pattern);
        Files.walkFileTree(start, finder);
        return finder.getResults();
    }
    
    /**
     * Internal class that extends the SimpleFileVisitor class that implements
     * the actual search.
     * 
     * @author L. Craig Carpenter
     *
     */
    public static class Finder extends SimpleFileVisitor<Path> {
        
        /**
         * Internal PathMatcher object.
         */
        private final PathMatcher _matcher;
        
        /**
         * Accumulator saving the list of matches found on the file system.
         */
        private List<Path> _matches = null;
        
        /**
         * Constructor setting up the search.
         * 
         * @param pattern The global search pattern to utilize for the search.
         * @throws IOException Thrown if the client-supplied pattern is not
         * defined.
         */
        public Finder(URI uri, String pattern) throws IOException {
            if ((pattern == null) || (pattern.isEmpty())) {
                throw new IOException("Usage error:  Search pattern not defined.");
            }
            if (uri != null) {
            	try {
            		// It seems different platforms implement this functionality 
            		// slightly differently.  JBoss (and some versions of Wildfly) 
            		// were able to determine the file system from a URI containing 
            		// the full path.  Tomcat was only able to obtain the FileSystem 
            		// if the path was set to the root file system (i.e. '/').  The 
            		// error was "IllegalStateException: Path component 
            		// should be '/'"  Logic updated to retrieve the scheme 
            		// from the input URI, add the "root" (i.e. '/') and then
            		// combine them into a URI.
            		URI scheme = new URI(
            				uri.getScheme().toString() + "://" + "/");
	            	_matcher = FileSystems.getFileSystem(scheme).getPathMatcher(
	                        "glob:" + pattern);
            	}
            	catch (URISyntaxException use) {
            		throw new IOException("Unable to construct the URI used to "
            				+ "look up the target file system.  Input URI [ "
            				+ uri.toString()
            				+ " ].  Exception message => [ "
            				+ use.getMessage()
            				+ " ].");
            	}
            }
            else {
            	throw new IOException("Unable to construct the URI used to "
            			+ "look up the target file system.  The input URI "
            			+ "is null.");
            }
        }
        
        /** 
         * Compares the glob pattern against the file and/or directory name.
         * 
         * @param file The file to perform the comparison against.
         */
        public void find(Path file) {
            Path name = file.getFileName();
            if ((name != null) && (_matcher.matches(name))) {
                if (_matches == null) {
                    _matches = new ArrayList<Path>();
                }
                _matches.add(file);
            }
        }
        
        /**
         * Accessor method for the results of the search.
         * 
         * @return Any results that were accumulated during the search 
         * (may be null). 
         */
        public List<Path> getResults() {
            return _matches;
        }
        
        /**
         * Invoke the pattern matching method on each directory in the file 
         * tree.
         */
        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                BasicFileAttributes attrs) {
            find(dir);
            return FileVisitResult.CONTINUE;
        }
        
        /**
         * Invoke the pattern matching method on each file in the file tree.
         */
        @Override
        public FileVisitResult visitFile(
                Path file,
                BasicFileAttributes attrs) {
            find(file);
            return FileVisitResult.CONTINUE;
        }
        
        /**
         * If the file visit failed issue an informational message to System.err
         */
        @Override
        public FileVisitResult visitFileFailed(Path file,
                IOException exc) {
            System.err.println("WARN:  Find command failed visiting file.  " 
                    + "Error message [ " 
                    + exc.getMessage()
                    + " ].");
            exc.printStackTrace();
            return FileVisitResult.CONTINUE;
        }
    }
    
    /**
     * If clients did not supply the "scheme" for the URI, this method is 
     * invoked to generate a URI with the local file system scheme.
     * 
     * @param uri The input URI (which was lacking a scheme).
     * @return Newly constructed URI pointing to the local file system.
     */
    protected static URI getURI(Path p, URI uri) {
        URI newURI = null;
        if (uri != null) {
            try {
                /*
                System.out.println("Creating URI => "
                        + "scheme [ "
                        + uri.getScheme()
                        + " ], authority [ "
                        + uri.getAuthority()
                        + " ], path [ "
                        + p.toString()
                        + " ], query [ "
                        + uri.getQuery()
                        + " ] fragment [ "
                        + uri.getFragment()
                        + " ].");
                */
                newURI = new URI(
                        uri.getScheme(), 
                        uri.getAuthority(), 
                        p.toString(),
                        uri.getQuery(), 
                        uri.getFragment());
            }
            // This exception can never be thrown here so just eat it.
            catch (URISyntaxException use) { }
        }
        return newURI;
    }
    
    /**
     * Convert a list of <code>Path</code> objects into a list of 
     * <code>URI</code> objects. 
     * 
     * @param paths List of Path objects.
     * @param uri The base URI.
     * @return A list of URI objects.  This method may return an empty
     * list, but it will not return null.
     */
    public static List<URI> toURIList(List<Path> paths, URI uri) {
        List<URI> uris = new ArrayList<URI>();
        if ((paths != null) && (!paths.isEmpty())) {
            for (Path p : paths) {
                uris.add(getURI(p, uri));
            }
        }
        return uris;
    }
    
    /**
     * The intention of this method is to accept a <code>URI</code> that 
     * points to a directory.  This method will then walk the directory tree 
     * returning a list of all files in the tree.  If the <code>URI</code> is 
     * not supplied the file will return a list containing a single 
     * <code>URI</code> object pointing to the file.
     * 
     * @param uri URI identifying a target directory. 
     * @return List of URI objects pointing to files that fall below the 
     * target directory.  
     * @throws IOException Thrown in conjunction with any issues walking the 
     * file tree.
     */
    public static List<URI> listFiles(URI uri) throws IOException {
        Path path= Paths.get(uri);
        final List<Path> files=new ArrayList<>();
        try {
             Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
             @Override
             public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                  if(!attrs.isDirectory()){
                       files.add(file);
                  }
                  return FileVisitResult.CONTINUE;
              }
             });
        } 
        catch (IOException e) {
             e.printStackTrace();
        }
              
        return toURIList(files, uri);
    }
}
