package ca.crim.nlp.pacte.client;

import org.json.JSONObject;

import ca.crim.nlp.pacte.QuickConfig;
import ca.crim.nlp.pacte.QuickConfig.USERTYPE;

public class Admin {
	private QuickConfig poCfg = null;

	public Admin(QuickConfig toConfig) {
		poCfg = toConfig;
	}

	public String listAllUsers() {
		String lsReturn = null;

		lsReturn = poCfg.getRequest(poCfg.getAuthenUrl() + "psc-users-permissions-management/Users/users",
				USERTYPE.PSCAdmin, null);

		return lsReturn;
	}

	/**
	 * Reset the password of a user
	 * 
	 * @param tsUsername
	 * @param tsOldPassword
	 * @param tsNewPassword
	 * @return
	 */
	public boolean resetPassword(String tsUsername, String tsOldPassword, String tsNewPassword) {
		String lsReturn = null;

		poCfg.setCustomUser(tsUsername, tsOldPassword);

		lsReturn = poCfg.putRequest(poCfg.getAuthenUrl() + "/psc-users-permissions-management/Users/myPassword",
				"{\"password\":\"" + tsNewPassword + "\"}", USERTYPE.CustomUser);

		if (lsReturn != null && lsReturn != "") {
			if (lsReturn.contains("\"id\":")) {
				System.out.println(lsReturn);
			}
		}

		return false;
	}

	/**
	 * 
	 * @param tsUsername
	 * @param tsPassword
	 * @param tsPrenom
	 * @param tsNom
	 * @return
	 */
	public String createUser(String tsUsername, String tsPassword, String tsPrenom, String tsNom) {
		String lsReturn = "";

		// Ajouter un nouvel utilisateur
		lsReturn = poCfg
				.postRequest(poCfg.getPacteBackend() + "PlatformUsers/platformUser",
						"{\"password\": \"" + tsPassword + "\",\"firstName\":\"" + tsPrenom + "\",\"lastName\":\""
								+ tsNom + "\",\"email\":\"" + tsUsername + "\", \"jwtAudience\": [\"Pacte\"]}",
						USERTYPE.PacteAdmin);

		if (lsReturn != null && !lsReturn.isEmpty() && lsReturn.toLowerCase().contains("userprofileid")) {
			if (poCfg.getVerbose()) {
				System.out.println("Utilisateur " + tsUsername + " a été créé!");
				System.out.println(lsReturn);
			}
			return lsReturn.substring(lsReturn.indexOf("userProfileId\":\"") + 16,
					lsReturn.indexOf("\"", lsReturn.indexOf("userProfileId\":\"") + 16));

		} else if (poCfg.getVerbose()) {
			if (lsReturn.toLowerCase().contains("conflict"))
				System.err.println("Utilisateur " + tsUsername + " existant! (possiblement avec d'autres accès)");

			else if (lsReturn.toLowerCase().contains("Unauthorized"))
				System.out.println("Accès administrateur invalides!");
		}

		return null;
	}

	/**
	 * Delete the configured custom account
	 * 
	 * @param tsUserID
	 * @return
	 */
	public boolean deleteUser() {
		String lsId = null;

		// Delete the user
		lsId = checkUser(poCfg.getUserCredential(USERTYPE.CustomUser).getUsername(),
				poCfg.getUserCredential(USERTYPE.CustomUser).getPassword());

		poCfg.deleteRequest(poCfg.getPacteBackend() + "PlatformUsers/platformUser/" + lsId,
				USERTYPE.CustomUser, null);

		lsId = null;
		lsId = checkUser(poCfg.getUserCredential(USERTYPE.CustomUser).getUsername(),
				poCfg.getUserCredential(USERTYPE.CustomUser).getPassword());

		return (lsId == null);
	}

	/**
	 * Verify if a user exists
	 * 
	 * @param tsUsername
	 * @param tsPassword
	 * @return Unique ID of user, Null is non-existant
	 */
	public String checkUser(String tsUsername, String tsPassword) {
		String lsReturn = "";

		// Se logger et obtenir un token
		poCfg.setCustomUser(tsUsername, tsPassword);

		lsReturn = poCfg.getRequest(poCfg.getPacteBackend() + "PlatformUsers/myPlatformUserContacts",
				USERTYPE.CustomUser, null);

		if (lsReturn != null && !lsReturn.isEmpty() && !lsReturn.contains("Forbidden")
				&& !lsReturn.contains("Unauthorized")) {
			JSONObject loJson = new JSONObject(lsReturn);
			System.out.println("Utilisateur " + loJson.getJSONObject("user").getString("userProfileId") + " existant.");
			return loJson.getJSONObject("user").getString("userProfileId");
		} else
			return null;

	}

	/**
	 * 
	 * @param tsUserID1
	 * @param tsUserID2
	 * @return
	 */
	public boolean addContact(String tsUserID) {
		String lsReturn = null;

		lsReturn = poCfg.postRequest(poCfg.getPacteBackend() + "PlatformUsers/myPlatformUserContact",
				"{\"contactUserProfileId\": \"" + tsUserID + "\"}", USERTYPE.CustomUser);

		if (lsReturn.contains("{\"contactStatus\":\""))
			return true;

		return false;
	}

}
