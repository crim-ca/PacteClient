package ca.crim.nlp.pacte.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ca.crim.nlp.pacte.QuickConfig;

public class CorpusTest {
	@Rule
	public TemporaryFolder poTestFolder = new TemporaryFolder();

	/**
	 * Create, populate and delete a corpus.
	 */
	@Test
	public void corpusLifeCycle() throws InterruptedException {
		String lsNewCorpusName = UUID.randomUUID().toString() + UUID.randomUUID().toString();
		String lsCorpusID = null;
		String lsReturn = null;
		String lsDociID = null;
		String lsGroupID = null;

		Corpus loCorpus = new Corpus(new QuickConfig());

		// Create the corpus
		System.out.print("Creating new corpus... ");
		lsReturn = loCorpus.createCorpus(lsNewCorpusName, "fr_fr");
		if (lsReturn != null && !lsReturn.isEmpty())
			lsCorpusID = lsReturn;
		assertNotNull(lsCorpusID);
		System.out.println("Created!");

		// Populate
		System.out.print("Adding document... ");
		lsDociID = loCorpus.addDocument(lsCorpusID, "bla bla bla", "bla", "none", "fr_fr");
		assertNotNull(lsDociID);
		System.out.println("Added!");

		// Create annotation group
		System.out.print("Creating annotation group... ");
		lsGroupID = loCorpus.createBucket(lsCorpusID, UUID.randomUUID().toString());
		assertNotNull(lsGroupID);
		System.out.println("Created!");

		// TODO: Removing group

		// Remove document
		Thread.sleep(500); //
		System.out.print("Deleting document... ");
		assertTrue(loCorpus.getDocument(lsCorpusID, lsDociID).getTitle().equals("bla"));
		System.out.println("Deleted!");

		// Delete
		System.out.print("Deleting corpus...");
		assertTrue(loCorpus.deleteCorpus(lsCorpusID));
		lsReturn = loCorpus.getCorpusId(lsNewCorpusName);
		assertNull(lsReturn);
		assertNull(loCorpus.getCorpusId(lsNewCorpusName));
		System.out.println("Deleted!");
		System.out.println("Done!");
	}

	/**
	 * Create the test user
	 */
	@Before
	public void checkTestSubject() {
		SampleBuilder.createTestingUser();
	}

	/**
	 * Export the sample corpus in a temporary folder.
	 * 
	 * @return The path for the exported corpus
	 * @throws IOException
	 */
	@Test
	public String exportCorpus() throws IOException {
		String lsCorpusId = null;
		File loExportPath = poTestFolder.newFolder();
		List<String> lasGroupList = new ArrayList<String>();
		Corpus loCorpus = new Corpus(new QuickConfig());

		lsCorpusId = SampleBuilder.smallCorpus(loCorpus);
		assertNotNull(lsCorpusId);

		loCorpus.exportToDisk(lsCorpusId, loExportPath.getAbsolutePath(), lasGroupList);

		// Stuff exported?
		assertTrue(loExportPath.list().length > 0);

		// Only two groups?

		// All documents exported?

		return loExportPath.getAbsolutePath();
	}

	@Test
	public void importCorpus() throws IOException {
		String lsCorpusId = null;
		Corpus loCorpus = new Corpus(new QuickConfig());
		File loSourcePath = null;

		// Export the corpus before running the test
		loSourcePath = new File(exportCorpus());

		lsCorpusId = loCorpus.importCorpus(loSourcePath.getAbsolutePath());
		assertNotNull(lsCorpusId);

		assertTrue(loCorpus.getSize(lsCorpusId) >= 2);

		// Delete imported corpus after successful test
		loCorpus.deleteCorpus(lsCorpusId);
	}
}
