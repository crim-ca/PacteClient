package ca.crim.nlp.pacte.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class SchemaDataTest {

	@Test
	public void loadTest() {
		String lsTestSchema = null;
		SchemaData loSchema = null;
		
		try {
			lsTestSchema = new String(
                    Files.readAllBytes(Paths.get(
                            ClassLoader.class.getResource("/ca/crim/nlp/pacte/document.json").toURI())),
                    Charset.forName("UTF-8"));
			assertNotNull(lsTestSchema);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return;
        }
		loSchema = new SchemaData(lsTestSchema);
		
		assertNotNull(loSchema);
		assertEquals("document", loSchema.TargetType);
		assertEquals("document", loSchema.SchemaType);
	}
}
