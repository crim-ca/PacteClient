package ca.crim.nlp.pacte;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ca.crim.nlp.pacte.QuickConfig.SCHEMA_TARGET;
import ca.crim.nlp.pacte.QuickConfig.USERTYPE;

public class QuickConfigTest {

	@Test
	public void testTargetSchema() {
		QuickConfig loCfg = new QuickConfig();
		assertNotNull(loCfg.getTargetSchema(SCHEMA_TARGET.CORPUS));	
		assertNotNull(loCfg.getTargetSchema(SCHEMA_TARGET.DOCUMENT));	
		assertNotNull(loCfg.getTargetSchema(SCHEMA_TARGET.DOCUMENT_SURFACE1D));	
	}
	
	@Test
	public void testEmptyURL() {
		QuickConfig loCfg = null;

		try {
			loCfg = new QuickConfig(null, "1", "2", false, 1);
		} catch (Exception e) {
		}
		assertNull(loCfg);

		try {
			loCfg = new QuickConfig("", "1", "2", false, 1);
		} catch (Exception e) {
		}
		assertNull(loCfg);
	}

	@Test
	public void testEmptyUser() {
		QuickConfig loCfg = null;
		try {
			loCfg = new QuickConfig("https://", null, "2", false, 1);
		} catch (Exception e) {
		}
		assertNull(loCfg);

		try {
			loCfg = new QuickConfig("https://", "", "2", false, 1);
		} catch (Exception e) {
		}
		assertNull(loCfg);
	}

	@Test
	public void testCredentials() {
		QuickConfig loCfg = null;

		loCfg = new QuickConfig("https://", "1", "2", "3", "4", "5", "6", false, 1, null);

		assertNotNull(loCfg.poCred.get(USERTYPE.CustomUser));
		assertNotNull(loCfg.poCred.get(USERTYPE.PacteAdmin));
		assertNotNull(loCfg.poCred.get(USERTYPE.PSCAdmin));

		assertEquals("1", loCfg.poCred.get(USERTYPE.PSCAdmin).getUsername());
		assertEquals("2", loCfg.poCred.get(USERTYPE.PSCAdmin).getPassword());

		assertEquals("3", loCfg.poCred.get(USERTYPE.PacteAdmin).getUsername());
		assertEquals("4", loCfg.poCred.get(USERTYPE.PacteAdmin).getPassword());

		assertEquals("5", loCfg.poCred.get(USERTYPE.CustomUser).getUsername());
		assertEquals("6", loCfg.poCred.get(USERTYPE.CustomUser).getPassword());
	}

	@Test
	public void testDefaultAdminConfig() {
		QuickConfig loCfg = new QuickConfig();
		
		assertNotNull(loCfg.getToken(loCfg.getUserCredential(USERTYPE.PacteAdmin)));
                assertNotNull(loCfg.getToken(loCfg.getUserCredential(USERTYPE.PSCAdmin)));           
	}
}
