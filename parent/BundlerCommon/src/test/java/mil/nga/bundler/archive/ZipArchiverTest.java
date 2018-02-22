package mil.nga.bundler.archive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import mil.nga.bundler.ArchiveElementFactory;
import mil.nga.bundler.archive.ArchiveFactory;
import mil.nga.bundler.types.ArchiveType;
import mil.nga.bundler.interfaces.BundlerConstantsI;
import mil.nga.bundler.interfaces.BundlerI;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.bundler.exceptions.UnknownArchiveTypeException;
import mil.nga.bundler.exceptions.ArchiveException;

import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * jUnit tests for the ZIP archiver.
 * 
 * @author carpenlc
 */
public class ZipArchiverTest extends ArchiveTest {

	public String _archiveFilename1 = "test_archive_1";
	public String _archiveFilename2 = "test_archive_2";
	
	/**
	 * This method tests that the ZIP archiver can archive a directory 
	 * and all files contained within, maintaining directory integrity.
	 * 
	 * @throws IOException Exception thrown if there are problems writing
	 * the output archive file.  Exceptions will fail the test.
	 */
	@Test
	public void testBundler1() throws ArchiveException, IOException {
	
		// Build the path to the output file
		StringBuilder sb = new StringBuilder();
		sb.append(ArchiveTest._tempDir);
		sb.append(File.separator);
		sb.append(this._archiveFilename1);
		this._archiveFilename1 = sb.toString();
		
		ArchiveFactory factory = ArchiveFactory.getInstance();
		try {
			
			ArchiveElementFactory archiveEF = new ArchiveElementFactory(awsProps);
			List<ArchiveElement> elems = archiveEF.getArchiveElements(ArchiveTest._dirToArchive, "/replace");
			
			BundlerI bundler = factory.getBundler(ArchiveType.ZIP);
			System.out.println(this._archiveFilename1);
			Path p = Paths.get(this._archiveFilename1);
			bundler.bundle(elems, p.toUri());
		
			System.out.println(p.toString());
			String outputFile = p.toString() + "." + ArchiveType.ZIP.getText();
			Path p2 = Paths.get(outputFile);
			assertTrue(Files.exists(p2));
			double bytes = Files.size(p2);
			System.out.println("File size [ " + bytes + " ].");
			
		}
		catch (UnknownArchiveTypeException uae) {
			
			// We *should* never get this exception
			uae.printStackTrace();

		}
	}
	
	
	/**
	 * This method tests that the ZIP archiver can archive each file in 
	 * an input list of files.  
	 * 
	 * @throws IOException Exception thrown if there are problems writing
	 * the output archive file.  Exceptions will fail the test.
	 */
	@Test
	public void testBundler2() throws ArchiveException, IOException {
	
		// Build the path to the output file
		StringBuilder sb = new StringBuilder();
		sb.append(ArchiveTest._tempDir);
		sb.append(File.separator);
		sb.append(this._archiveFilename2);
		this._archiveFilename2 = sb.toString();
		List<String> list = super.getFileList();
		ArchiveFactory factory = ArchiveFactory.getInstance();
		try {
			
			ArchiveElementFactory archiveEF = new ArchiveElementFactory(awsProps);
			List<ArchiveElement> elems = archiveEF.getArchiveElements(list);
			
			BundlerI bundler = factory.getBundler(ArchiveType.ZIP);
			
			System.out.println(this._archiveFilename2);
			Path p = Paths.get(this._archiveFilename2);
			bundler.bundle(elems, p.toUri());
		
			String outputFile = p.toString() + "." + ArchiveType.ZIP.getText();
			Path p2 = Paths.get(outputFile);
			assertTrue(Files.exists(p2));
			double bytes = Files.size(p2);
			System.out.println("File size [ " + bytes + " ].");
		}
		catch (UnknownArchiveTypeException uae) {
			
			// We *should* never get this exception
			uae.printStackTrace();

		}
	}
	
}
