package ca.crim.nlp.pacte.client;

import java.util.UUID;

public class SampleBuilder {

	public static String smallCorpus(Corpus toCorpus) {
		String lsCorpusId = null;
		lsCorpusId = toCorpus.createCorpus("exportCorpusTest-79871875649756", "fr_fr,en_en");

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
}
