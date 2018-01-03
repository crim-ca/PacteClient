package ca.crim.nlp.pacte.client;

import ca.crim.nlp.pacte.QuickConfig;
import ca.crim.nlp.pacte.QuickConfig.Credential;
import ca.crim.nlp.pacte.QuickConfig.USERTYPE;
import ca.crim.nlp.pacte.UnitTestConstants;

public class SampleBuilder {

	/**
	 * Create a small test corpus if it does not already exists.
	 * 
	 * @param toCorpus
	 *            A preconfigured corpus instance
	 * @return The new or existing corpus id
	 */
	public static String smallCorpus(Corpus toCorpus) {
		String lsCorpusId = null;
		lsCorpusId = toCorpus.getCorpusId(UnitTestConstants.TESTCORPUS);

		// TODO check for corpus integrity before returning existing id

		if (lsCorpusId != null)
			return lsCorpusId;

		// Create the new corpus
		lsCorpusId = toCorpus.createCorpus(UnitTestConstants.TESTCORPUS, "fr_fr,en_en");

		if (lsCorpusId != null) {
			// Documents
			toCorpus.addDocument(lsCorpusId, "bla bla bla", "testExport1", "yep1", "fr_fr");
			toCorpus.addDocument(lsCorpusId, "bli bli bli", "testExport2", "yep2", "fr_fr");

			// Groups
			toCorpus.createBucket(lsCorpusId, "group1");
			toCorpus.createBucket(lsCorpusId, "group2");
			toCorpus.createBucket(lsCorpusId, "group3");

			// TODO Ajouter des schémas + annotations
		}

		return lsCorpusId;
	}

	/**
	 * Create the testing user on the PACTE platform defined in the configuration
	 * file
	 * 
	 * @return True if the user exists
	 */
	public static boolean createTestingUser() {
		QuickConfig loCfg = new QuickConfig();
		Admin loAdmin = new Admin(loCfg);
		Credential loUser = loCfg.getUserCredential(USERTYPE.CustomUser);
		String lsUserId = null;

		lsUserId = loAdmin.checkUser(loUser.getUsername(), loUser.getPassword());

		if (lsUserId == null) {
			loAdmin.createUser(loUser.getUsername(), loUser.getPassword(), "TestUser", "011");
			lsUserId = loAdmin.checkUser(loUser.getUsername(), loUser.getPassword());
		}

		return lsUserId != null;
	}
}
