package mil.nga.bundler.archive;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import mil.nga.bundler.ArchiveElementFactory;
import mil.nga.bundler.archive.ArchiveFactory;
import mil.nga.bundler.types.ArchiveType;
import mil.nga.bundler.interfaces.BundlerI;
import mil.nga.bundler.model.ArchiveElement;
import mil.nga.bundler.exceptions.ArchiveException;
import mil.nga.bundler.exceptions.UnknownArchiveTypeException;

import org.junit.Test;

public class BZip2ArchiveTest extends ArchiveTest {

	public String _archiveFilename1 = "bzip2_archive_1";
	public String _archiveFilename2 = "bzip2_archive_2";
	
	/**
	 * This method tests that the GZIP archiver can archive a directory 
	 * and all files contained within, maintaining directory integrity.
	 * 
	 * @throws ArchiveException Thrown if there are problems validating the
	 * input data.  Exceptions will fail the test.
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
			
			ArchiveElementFactory archiveEF = new ArchiveElementFactory();
			List<ArchiveElement> elems = archiveEF.getArchiveElements(ArchiveTest._dirToArchive, "/replace");
			
			BundlerI bundler = factory.getBundler(ArchiveType.BZIP2);
			
			Path p = Paths.get(this._archiveFilename1);
			bundler.bundle(elems, p.toUri());
		
			System.out.println(p.toString());
			String outputFile = p.toString() + "." 
					+ ArchiveType.TAR.getText() 
					+ "." + ArchiveType.BZIP2.getText();
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
	 * This method tests that the GZIP archiver can archive a list of input
	 * files.
	 * 
	 * @throws ArchiveException Thrown if there are problems validating the
	 * input data.  Exceptions will fail the test.
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
			
			ArchiveElementFactory archiveEF = new ArchiveElementFactory();
			List<ArchiveElement> elems = archiveEF.getArchiveElements(list);
			
			BundlerI bundler = factory.getBundler(ArchiveType.BZIP2);
			
			Path p = Paths.get(this._archiveFilename2);
			bundler.bundle(elems, p.toUri());
			
			System.out.println(p.toString());
			String outputFile = p.toString() + "." 
					+ ArchiveType.TAR.getText() 
					+ "." + ArchiveType.BZIP2.getText();
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
