package ca.crim.nlp.pacte.client;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;

import ca.crim.nlp.pacte.QuickConfig;
import ca.crim.nlp.pacte.QuickConfig.USERTYPE;
import ca.crim.nlp.pacte.UnitTestConstants;

public class SampleBuilder {

    /**
     * Create a small test corpus if it does not already exists.
     * 
     * @param toCorpus
     *            A preconfigured corpus instance
     * @return The new or existing corpus id
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String smallCorpus(Corpus toCorpus) {
        String lsCorpusId = null;
        String lsTranscodeGroup = null;
        String lsTrancodeSchema = null;
        String lsDocId = null;
        String lsAnnotationId = null;
        int lniCptFail = 0;
        lsCorpusId = toCorpus.getCorpusId(UnitTestConstants.TESTCORPUS);
        String lsCurrentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        // TODO check for corpus integrity before returning existing id

        if (lsCorpusId != null)
            return lsCorpusId;

        // Create the new corpus
        lsCorpusId = toCorpus.createCorpus(UnitTestConstants.TESTCORPUS, "fr_fr,en_en");

        if (lsCorpusId != null) {
            lsTranscodeGroup = toCorpus.getGroupId(UnitTestConstants.TRANSCODEGROUP, lsCorpusId);
            while (lsTranscodeGroup == null && lniCptFail < 10) {
                pleaseWait();
                lsTranscodeGroup = toCorpus.getGroupId(UnitTestConstants.TRANSCODEGROUP, lsCorpusId);
                lniCptFail++;
            }

            if (lsTranscodeGroup == null) {
                System.err.println("Cannot find transcoder group id");
                return null;
            }

            // Register schemas
            try {
                lsTrancodeSchema = new String(
                        Files.readAllBytes(Paths.get(
                                ClassLoader.class.getResource("/ca/crim/nlp/pacte/client/document_meta.json").toURI())),
                        Charset.forName("UTF-8"));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                return null;
            }

            String lsSchemaId = toCorpus.getSchemaId(new JSONObject(lsTrancodeSchema).getString("schemaType"), "", "");
            if (lsSchemaId == null)
                lsSchemaId = toCorpus.registerSchema(lsTrancodeSchema);
            toCorpus.copySchemaToGroup(lsSchemaId, lsCorpusId, lsTranscodeGroup);

            // Documents and their metadata
            lsDocId = toCorpus.addDocument(lsCorpusId, "bla bla bla", "testExport1", "yep1", "fr_fr");
            pleaseWait();
            lsAnnotationId = toCorpus.addAnnotation(lsCorpusId, lsTranscodeGroup,
                    "{\"document_size\":11,\"source\":\"tamere.zip\",\"file_edit_date\":\"" + lsCurrentTime
                            + "\",\"detectedLanguageProb\":99.99972436012376,"
                            + "\"file_type\":\"text/plain; charset=UTF-8\"," + "\"_documentID\":\"" + lsDocId + "\","
                            + "\"file_path\":\"/\",\"indexedLanguage\":\"fr_FR\",\"schemaType\":\"DOCUMENT_META\","
                            + "\"file_name\":\"1.txt\",\"file_encoding\":\"UTF-8\",\"_corpusID\":\"" + lsCorpusId
                            + "\",\"detectedLanguage\":\"fr_FR\"," + "\"file_size\":12,\"file_creation_date\":\""
                            + lsCurrentTime + "\",\"file_extension\":\".txt\"}");
            if (lsAnnotationId == null)
                System.err.println("Empty annotation 1 ");

            lsDocId = toCorpus.addDocument(lsCorpusId, "bli bli bli bli", "testExport2", "yep2", "fr_fr");
            pleaseWait();
            lsAnnotationId = toCorpus.addAnnotation(lsCorpusId, lsTranscodeGroup,
                    "{\"document_size\":15,\"source\":\"tamere.zip\",\"file_edit_date\":\"" + lsCurrentTime
                            + "\",\"detectedLanguageProb\":99.99972436012376,"
                            + "\"file_type\":\"text/plain; charset=UTF-8\"," + "\"_documentID\":\"" + lsDocId + "\","
                            + "\"file_path\":\"/\",\"indexedLanguage\":\"fr_FR\",\"schemaType\":\"DOCUMENT_META\","
                            + "\"file_name\":\"2.txt\",\"file_encoding\":\"UTF-8\",\"_corpusID\":\"" + lsCorpusId
                            + "\",\"detectedLanguage\":\"fr_FR\"," + "\"file_size\":16,\"file_creation_date\":\""
                            + lsCurrentTime + "\",\"file_extension\":\".txt\"}");
            if (lsAnnotationId == null)
                System.err.println("Empty annotation 2 ");

            // Groups
            toCorpus.createBucket(lsCorpusId, "group1");
            toCorpus.createBucket(lsCorpusId, "group2");
            toCorpus.createBucket(lsCorpusId, "group3");

            // TODO Ajouter des schémas + annotations
        }

        return lsCorpusId;
    }

    /**
     * Create the testing user on the PACTE platform defined in the
     * configuration file
     * 
     * @return True if the user exists
     */
    public static boolean createTestingUser() {
        QuickConfig loCfg = new QuickConfig();
        Admin loAdmin = new Admin(loCfg);
        ca.crim.nlp.pacte.Credential loUser = loCfg.getUserCredential(USERTYPE.CustomUser);
        String lsUserId = null;

        lsUserId = loAdmin.checkUser(loUser.getUsername(), loUser.getPassword());

        if (lsUserId == null) {
            loAdmin.createUser(loUser.getUsername(), loUser.getPassword(), "TestUser", "011");
            lsUserId = loAdmin.checkUser(loUser.getUsername(), loUser.getPassword());
        }

        return lsUserId != null;
    }

    private static void pleaseWait() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
