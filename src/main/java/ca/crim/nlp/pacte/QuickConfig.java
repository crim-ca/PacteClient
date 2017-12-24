package ca.crim.nlp.pacte;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

public class QuickConfig {
	String psBaseURLAuthen = "";
	String psBaseURLPacteBE = "";
	Integer pniTokenRenewDelay = -12;

	private boolean pbVerbose = true;

	public enum USERTYPE {
		PSCAdmin, PacteAdmin, CustomUser
	};

	Map<USERTYPE, Credential> poCred = new HashMap<USERTYPE, Credential>();

	// Creating an instance of HttpClient.
	CloseableHttpClient httpclient = HttpClients.createDefault();

	/**
	 * New configuration with admin and user level credentials
	 * 
	 * @param tsBasePacteUrl
	 * @param tsAdminPSCUsername
	 * @param tsAdminPSCPassword
	 * @param tsAdminPacteUsername
	 * @param tsAdminPactePassword
	 * @param tsCustomUser
	 * @param tsCustomPassword
	 * @param tbVerbose
	 */
	public QuickConfig(String tsBasePacteUrl, String tsAdminPSCUsername, String tsAdminPSCPassword,
			String tsAdminPacteUsername, String tsAdminPactePassword, String tsCustomUser, String tsCustomPassword,
			boolean tbVerbose, int tniTokenRenewDelay) {
		psBaseURLAuthen = tsBasePacteUrl.endsWith("/") ? tsBasePacteUrl : tsBasePacteUrl + "/";
		psBaseURLPacteBE = psBaseURLAuthen + "pacte-backend/";
		pniTokenRenewDelay = tniTokenRenewDelay;

		// PSC admin
		poCred.put(USERTYPE.PSCAdmin, new Credential(tsAdminPSCUsername, tsAdminPSCPassword, pniTokenRenewDelay));

		// Pacte admin
		poCred.put(USERTYPE.PacteAdmin, new Credential(tsAdminPacteUsername, tsAdminPactePassword, pniTokenRenewDelay));

		// Pacte custome user
		poCred.put(USERTYPE.CustomUser, new Credential(tsCustomUser, tsCustomPassword, pniTokenRenewDelay));

		pbVerbose = tbVerbose;
	}

	/**
	 * New configuration with user level credentials
	 * 
	 * @param tsBasePacteUrl
	 *            Mandatory url to acessible PACTE platform.
	 * @param tsCustomUser
	 *            Mandatory username to access the platform.
	 * @param tsCustomPassword
	 *            User's password.
	 * @param tbVerbose
	 *            True if you want detailed processing messages.
	 */
	public QuickConfig(String tsBasePacteUrl, String tsCustomUser, String tsCustomPassword, boolean tbVerbose,
			int tniTokenRenewDelay) {
		if (tsBasePacteUrl == null || tsBasePacteUrl.isEmpty())
			throw new IllegalArgumentException("PACTE url should not be null");

		if (tsCustomUser == null || tsCustomUser.isEmpty())
			throw new IllegalArgumentException("Username should not be null");

		psBaseURLAuthen = tsBasePacteUrl.endsWith("/") ? tsBasePacteUrl : tsBasePacteUrl + "/";
		psBaseURLPacteBE = psBaseURLAuthen + "pacte-backend/";
		pniTokenRenewDelay = tniTokenRenewDelay;

		// Pacte custome user
		poCred.put(USERTYPE.CustomUser, new Credential(tsCustomUser, tsCustomPassword, pniTokenRenewDelay));

		pbVerbose = tbVerbose;
	}

	public void setCustomUser(String tsUsername, String tsPassword) {
		poCred.put(USERTYPE.CustomUser, new Credential(tsUsername, tsPassword, pniTokenRenewDelay));
	}

	public Credential getUserCredential(USERTYPE toType) {
		if (poCred.keySet().contains(toType))
			return poCred.get(toType);
		else
			return null;
	}

	/**
	 * Set a different route for credentials (for unusual backend configuration)
	 * 
	 * @param tsUrl
	 *            : Complete url to the authentication backend for tokens
	 */
	public void setAuthenUrl(String tsUrl) {
		psBaseURLAuthen = tsUrl.endsWith("/") ? tsUrl : tsUrl + "/";
	}

	public String getAuthenUrl() {
		return psBaseURLAuthen;
	}

	public String getPacteBackend() {
		return psBaseURLPacteBE;
	}

	/**
	 * Call a GET request with preconfigured user credentials.
	 * 
	 * @param tsTargetEndpoint
	 * @param toUsertype
	 * @param toParams
	 * @return
	 */
	public String getRequest(String tsTargetEndpoint, USERTYPE toUsertype, List<NameValuePair> toParams) {
		String lsReturn = "";
		URIBuilder loUriBuilder = null;

		try {
			loUriBuilder = new URIBuilder(tsTargetEndpoint);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		if (toParams != null)
			loUriBuilder.addParameters(toParams);

		HttpGet loGet = new HttpGet(tsTargetEndpoint);
		loGet.addHeader("Authorization", "Bearer " + poCred.get(toUsertype).getToken());
		loGet.addHeader("AuthorizationAudience", "Pacte");

		try {
			CloseableHttpResponse response = httpclient.execute(loGet);
			lsReturn = readInput(response.getEntity().getContent());

			if (pbVerbose || ((response.getStatusLine().getStatusCode() != 200)
					&& (response.getStatusLine().getStatusCode() != 204)))
				System.out.println("Response Status line :" + response.getStatusLine());

			response.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return lsReturn;
	}

	/**
	 * Call a DELETE request with preconfigured user credentials.
	 * 
	 * @param tsTargetEndpoint
	 * @param tsToken
	 * @param toParams
	 * @return
	 */
	public String deleteRequest(String tsTargetEndpoint, USERTYPE toUsertype, List<NameValuePair> toParams) {
		String lsReturn = "";
		URIBuilder loUriBuilder = null;

		try {
			loUriBuilder = new URIBuilder(tsTargetEndpoint);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		if (toParams != null)
			loUriBuilder.addParameters(toParams);

		HttpDelete loDel = new HttpDelete(tsTargetEndpoint);
		loDel.addHeader("Authorization", "Bearer " + poCred.get(toUsertype).getToken());
		loDel.addHeader("AuthorizationAudience", "Pacte");

		try {
			CloseableHttpResponse response = httpclient.execute(loDel);
			if (response.getEntity() != null)
				lsReturn = readInput(response.getEntity().getContent());

			if (pbVerbose || ((response.getStatusLine().getStatusCode() != 200)
					&& (response.getStatusLine().getStatusCode() != 204)))
				System.out.println("Response Status line :" + response.getStatusLine());

			response.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return lsReturn;
	}

	/**
	 * Call a POST request with preconfigured user credentials.
	 * 
	 * @param tsTargetEndpoint
	 * @param tsJson2Post
	 * @param toUsertype
	 * @return
	 */
	public String postRequest(String tsTargetEndpoint, String tsJson2Post, USERTYPE toUsertype) {
		HttpPost httpost = new HttpPost(tsTargetEndpoint);
		// EntityBuilder loBuilder = EntityBuilder.create();
		// HttpEntity entity = null;
		String lsResponse = "";

		// Ajouter le json
		if (tsJson2Post != null) {
			StringEntity postingString = new StringEntity(tsJson2Post, "UTF-8");
			httpost.setEntity(postingString);
			httpost.setHeader("Content-type", "application/json");
			httpost.setHeader("Accept", "application/json");
		}

		if (toUsertype != null) {
			httpost.setHeader("Authorization", "Bearer " + poCred.get(toUsertype).getToken());
			httpost.setHeader("AuthorizationAudience", "Pacte");
		}

		// Executing the request.
		try {
			CloseableHttpResponse response = httpclient.execute(httpost);
			if (response.getEntity() != null)
				lsResponse = readInput(response.getEntity().getContent());

			if (pbVerbose || ((response.getStatusLine().getStatusCode() != 200)
					&& (response.getStatusLine().getStatusCode() != 204)))
				System.out.println("Response Status line :" + response.getStatusLine());

			response.close();

		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return lsResponse;
	}

	/**
	 * Call a PUT request with preconfigured user credentials.
	 * 
	 * @param tsTargetEndpoint
	 * @param tsJson2Post
	 * @param toUsertype
	 * @return
	 */
	public String putRequest(String tsTargetEndpoint, String tsJson2Post, USERTYPE toUsertype) {
		HttpPut httput = new HttpPut(tsTargetEndpoint);

		String lsResponse = "";

		// Ajouter le json
		if (tsJson2Post != null) {
			StringEntity postingString = new StringEntity(tsJson2Post, "UTF-8");
			httput.setEntity(postingString);
			httput.setHeader("Content-type", "application/json");
			httput.setHeader("Accept", "application/json");
		}

		httput.setHeader("Authorization", "Bearer " + poCred.get(toUsertype).getToken());
		httput.setHeader("AuthorizationAudience", "Pacte");

		// Executing the request.
		try {
			CloseableHttpResponse response = httpclient.execute(httput);
			if (response.getEntity() != null)
				lsResponse = readInput(response.getEntity().getContent());

			if (pbVerbose || ((response.getStatusLine().getStatusCode() != 200)
					&& (response.getStatusLine().getStatusCode() != 204)))
				System.out.println("Response Status line :" + response.getStatusLine());

			response.close();

		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return lsResponse;
	}

	public String getJsonFeature(String tsJson, String tsFeature) {
		JSONObject obj = new JSONObject(tsJson);
		return obj.getString(tsFeature);
	}

	public void setVerbose(boolean tbVerbose) {
		pbVerbose = tbVerbose;
	}

	public boolean getVerbose() {
		return pbVerbose;
	}

	/**
	 * Read the input stream from http socket
	 * 
	 * @param in
	 * @return
	 */
	private String readInput(InputStream in) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder result = new StringBuilder();
		String line = "";

		try {
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result.toString();
	}

	public class Credential {
		private String psUsername = null;
		private String psPassword = null;
		private String psToken = null;
		private Date pdTokenCreation = new Date();
		private Integer pniTokenRenewDelay = null;

		Credential(String tsUsername, String tsPassword, int tniRenewHour) {
			psUsername = tsUsername;
			psPassword = tsPassword;
			pniTokenRenewDelay = tniRenewHour > 0 ? tniRenewHour * -1 : tniRenewHour;
		}

		public String getUsername() {
			return psUsername;
		}

		public String getPassword() {
			return psPassword;
		}

		/**
		 * Get the authentication token, renewing it after the delay.
		 * 
		 * @return User's token
		 */
		public String getToken() {
			Calendar ldElapsed = Calendar.getInstance();
			ldElapsed.add(Calendar.HOUR, pniTokenRenewDelay);

			if (psToken == null || pdTokenCreation.before(ldElapsed.getTime()))
				psToken = getToken(psUsername, psPassword);
			return psToken;
		}

		/**
		 * Get a specific user credential token
		 * 
		 * @param tsUsername
		 * @param tsPassword
		 * @return
		 */
		private String getToken(String tsUsername, String tsPassword) {
			String lsReturn = "";

			lsReturn = postRequest(psBaseURLAuthen + "psc-authentication-service/FormLogin/login", "{\"username\": \""
					+ tsUsername + "\",\"password\": \"" + tsPassword + "\",\"jwtAudience\": [\"Pacte\"]}", null);

			if (lsReturn != null && !lsReturn.isEmpty() && !lsReturn.toLowerCase().contains("unauthorized"))
				return getJsonFeature(lsReturn, "token");

			return null;
		}
	}
}
