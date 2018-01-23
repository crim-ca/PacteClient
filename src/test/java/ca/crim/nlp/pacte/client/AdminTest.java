package ca.crim.nlp.pacte.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ca.crim.nlp.pacte.Credential;
import ca.crim.nlp.pacte.QuickConfig;

public class AdminTest {

	@Ignore
	// FIXME : when deleting a user is available on the rest api
	public void testCreateDeleteUser() {
		String lsUsername = "user-" + UUID.randomUUID().toString() + "@test.com";
		String lsPwd = UUID.randomUUID().toString();
		String lsPrenom = "User";
		String lsNom = "Test";
		String lsUserId = null;

		QuickConfig loCfg = new QuickConfig();
		loCfg.setCustomUser(lsUsername, lsPwd);
		Admin loAdmin = new Admin(loCfg);
		lsUserId = loAdmin.createUser(lsUsername, lsPwd, lsPrenom, lsNom).getUserId();

		assertNotNull(lsUserId);
		assertEquals(36, lsUserId.length());
		assertNotNull(loAdmin.checkUser(lsUsername, lsPwd));

		loCfg.setCustomUser(lsUsername, lsPwd);
		loAdmin.deleteUser(lsUserId);
		assertNull(loAdmin.checkUser(lsUsername, lsPwd));
	}

	@Before
	public void checkCreateUsers() {
		String lsId1 = null;
		String lsId2 = null;
		String lsUsername1 = "testuser-unlinked1@test.com";
		String lsPwd1 = "secret";
		String lsUsername2 = "testuser-unlinked2@test.com";
		String lsPwd2 = "secret";
		
		Admin loAdmin = new Admin(new QuickConfig());
		lsId1 = loAdmin.checkUser(lsUsername1, lsPwd1);
		lsId2 = loAdmin.checkUser(lsUsername1, lsPwd1);

		if (lsId1 == null)
			loAdmin.createUser(lsUsername1, lsPwd1, "testingUser1", "testingUser1");

			if (lsId2 == null)
				loAdmin.createUser(lsUsername2, lsPwd2, "testingUser2", "testingUser2");
	}
	
	@Test
	public void testLinkUsers() {
		Credential loId1 = null;
		Credential loId2 = null;
		String lsUsername1 = UUID.randomUUID().toString();
		String lsPwd1 = UUID.randomUUID().toString();
		String lsUsername2 = UUID.randomUUID().toString();
		String lsPwd2 = UUID.randomUUID().toString();

		QuickConfig loCfg = new QuickConfig();
		Admin loAdmin = new Admin(loCfg);
		
		loId1 = loAdmin.createUser(lsUsername1, lsPwd1, UUID.randomUUID().toString(), UUID.randomUUID().toString());
		loId2 = loAdmin.createUser(lsUsername2, lsPwd2, UUID.randomUUID().toString(), UUID.randomUUID().toString());

		loCfg.setCustomUser(lsUsername1, lsPwd1);
                loAdmin.removeContact(loId2.getUserId());
		assertTrue(loAdmin.addContact(loId2.getUserProfileId()));
		assertTrue(loAdmin.removeContact(loId2.getUserProfileId()));
		
		loAdmin.deleteUser(loId1.getUserId());
		assertNull(loAdmin.checkUser(lsUsername1, lsPwd1));

		loCfg.setCustomUser(lsUsername2, lsPwd2);
		loAdmin.deleteUser(loId2.getUserId());
		assertNull(loAdmin.checkUser(lsUsername2, lsPwd2));
	}
}