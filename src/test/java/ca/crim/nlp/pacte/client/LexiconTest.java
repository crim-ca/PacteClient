package ca.crim.nlp.pacte.client;

import static org.junit.Assert.assertNotEquals;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ca.crim.nlp.pacte.QuickConfig;

public class LexiconTest {

    @Test
    public void checkLexique() {
        String lsIdLexique = null;
        String lsIdDomain = null;
        String lsIdConcept = null;
        String lsIdTerm = null;

        Lexicon loLex = new Lexicon(new QuickConfig());

        lsIdLexique = loLex.createLexicon("Test lexicon - " + System.currentTimeMillis());
        assertNotEquals("", lsIdLexique);

        // ajouter un domaine
        Map<String, String> loTitles = new HashMap<String, String>();
        loTitles.put("FR", "titre 1");
        loTitles.put("EN", "title 1");
        lsIdDomain = loLex.createDomain(lsIdLexique, "concept 1", null, loTitles);
        assertNotEquals("", lsIdDomain);

        // créer et lier un concept
        Map<String, String> loDescs = new HashMap<String, String>();
        loDescs.put("FR", "desc 1");
        loDescs.put("EN", "desc 1");
        Map<String, String> loExamples = new HashMap<String, String>();
        loExamples.put("FR", "concept 1");
        loExamples.put("EN", "concept 1");
        lsIdConcept = loLex.createConcept(lsIdLexique, "concept", loTitles, loExamples, loDescs);
        assertNotEquals("", lsIdConcept);

        loLex.linkDomainConcept(lsIdDomain, lsIdConcept);

        // ajouter des termes concurrents
        lsIdTerm = loLex.createTerm(lsIdLexique, "term1", "", "FR", null, null, null);
        assertNotEquals("", lsIdTerm);
        loLex.linkConceptTerm(lsIdConcept, lsIdTerm);
    }
}
