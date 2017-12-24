package ca.crim.nlp.pacte.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import ca.crim.nlp.pacte.QuickConfig;

public class AdminTest {
	private String psPacteUrl = "https://patx-pacte.crim.ca";

	@Test
	public void testCreateDeleteUser() {
		String lsUsername = "user1@test.com";
		String lsPwd = UUID.randomUUID().toString();
		String lsPrenom = "User";
		String lsNom = "One";
		String lsUserId = null;

		QuickConfig loCfg = new QuickConfig(psPacteUrl, "", "", "test@test.com", "secret", lsUsername, lsPwd, false, 1);
		Admin loAdmin = new Admin(loCfg);
		lsUserId = loAdmin.createUser(lsUsername, lsPwd, lsPrenom, lsNom);

		assertNotNull(lsUserId);
		assertNotNull(loAdmin.checkUser(lsUsername, lsPwd));

		loCfg.setCustomUser(lsUsername, lsPwd);
		loAdmin.deleteUser();
		assertNull(loAdmin.checkUser(lsUsername, lsPwd));
	}

	@Test
	public void testLinkUsers() {
		String lsId1 = null;
		String lsId2 = null;
		String lsUsername1 = UUID.randomUUID().toString();
		String lsPwd1 = UUID.randomUUID().toString();
		String lsUsername2 = UUID.randomUUID().toString();
		String lsPwd2 = UUID.randomUUID().toString();

		QuickConfig loCfg = new QuickConfig("https://patx-pacte.crim.ca", "", "", "test@test.com", "secret", "", "",
				true, 1);

		Admin loAdmin = new Admin(loCfg);		
		lsId1 = loAdmin.createUser(lsUsername1, lsPwd1, UUID.randomUUID().toString(), UUID.randomUUID().toString());
		lsId2 = loAdmin.createUser(lsUsername2, lsPwd2, UUID.randomUUID().toString(), UUID.randomUUID().toString());

		loCfg.setCustomUser(lsId1, lsPwd1);
		assertTrue(loAdmin.addContact(lsId2));
		loAdmin.deleteUser();
		assertNull(loAdmin.checkUser(lsUsername1, lsPwd1));

		loCfg.setCustomUser(lsId2, lsPwd2);
		loAdmin.deleteUser();
		assertNull(loAdmin.checkUser(lsUsername2, lsPwd2));
	}
}
