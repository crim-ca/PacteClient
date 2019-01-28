package ca.crim.nlp.pacte.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SchemaDataTest {

	@Test
	public void loadTest() {
		String lsTestSchema = null;
		SchemaData loSchema = null;

		try {
			lsTestSchema = new String(
					Files.readAllBytes(
							Paths.get(ClassLoader.class.getResource("/ca/crim/nlp/pacte/document.json").toURI())),
					Charset.forName("UTF-8"));
			assertNotNull(lsTestSchema);

		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			return;
		}

		loSchema = new SchemaData(lsTestSchema);

		assertNotNull(loSchema);
		assertEquals("document", loSchema.TargetType.name());
		assertEquals("document", loSchema.SchemaType);

		List<String> loSearch = new ArrayList<String>();
		loSearch.add("noop");
		loSchema.FeatureList.put("description", new FeatureDefinition("description", "String",
				"Description for the annotation", "", true, loSearch, false));
		SchemaData loNew = new SchemaData(loSchema.toString());
		assert(loNew.FeatureList.keySet().contains("description"));
	}

	@Test
	public void instantiateTest() {
		List<FeatureDefinition> loFeatures = new ArrayList<FeatureDefinition>();
		List<String> loSearch = new ArrayList<String>();

		loSearch.add("basic");
		loFeatures.add(new FeatureDefinition("commentaire", "string", "Notes sur l'annotation", "default", true,
				loSearch, true));

		SchemaData loS = new SchemaData(SchemaData.TARGET.document_surface1d, "test", loFeatures);
		assert (loS.FeatureList.containsKey("commentaire"));
		assertEquals(1, loS.FeatureList.size());
	}
}
