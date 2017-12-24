package ca.crim.nlp.pacte.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import ca.crim.nlp.pacte.QuickConfig;
import ca.crim.nlp.pacte.QuickConfig.USERTYPE;

public class Corpus {
	private QuickConfig poCfg = null;

	public Corpus(QuickConfig toConfig) {
		poCfg = toConfig;
	}

	/**
	 * Save a corpus' documents, groups and annotations to disk. Will not retain
	 * credentials.
	 * 
	 * @param tsCorpusId
	 * @param tsOuputPath
	 * @param tsExportGroupId
	 *            : Ids of group to export. If none listed, all groups are exported.
	 * @return True if exported with success, false if error during export.
	 */
	public boolean exportToDisk(String tsCorpusId, String tsOuputPath, List<String> tasExportGroupId) {
		String lsReturn = "";
		Map<String, String> lasBuckets = new HashMap<String, String>();

		// Structure du corpus (sauvegarder les groupes et leur id)
		lsReturn = poCfg.getRequest(poCfg.getPacteBackend() + "RACSProxy/corpora/" + tsCorpusId + "/structure",
				USERTYPE.CustomUser, null);
		if (lsReturn != null && !lsReturn.isEmpty()) {
			JSONObject loRet = new JSONObject(lsReturn);
			System.out.println(loRet.get("id"));
		}

		// Check if groups are in the structure, if not, exit.
		if (tasExportGroupId != null && !tasExportGroupId.isEmpty())
			for (String lsId : tasExportGroupId)
				if (!lasBuckets.keySet().contains(lsId)) {
					System.err.println("Missing group : " + lsId);
					return false;
				}

		// List documents

		// List annotation per group

		return false;
	}

	public String getCorpusId(String tsNomCorpus) {
		String lsIdCorpus = "";
		String lsReturn = "";

		lsReturn = poCfg.getRequest(poCfg.getPacteBackend() + "Corpora/corpora", USERTYPE.CustomUser, null);
		if (lsReturn != null && !lsReturn.isEmpty()) {
			int lniPos = lsReturn.toLowerCase().indexOf("\"title\":\"" + tsNomCorpus.toLowerCase() + "\"");
			if (lniPos >= 0) {
				lniPos = lsReturn.substring(0, lniPos).lastIndexOf("\"id\":\"") + 6;
				lsIdCorpus = lsReturn.substring(lniPos, lniPos + 36);
				System.out.println("Corpus " + tsNomCorpus + " (" + lsIdCorpus + ") a été trouvé!");
				return lsIdCorpus;
			}
		}
		return null;
	}

	/**
	 * Create a new corpus
	 * 
	 * @param tsNomCorpus
	 *            Corpus name
	 * @param tsLangage
	 *            List of comma separated values. ex: FR_fr, EN_en, ES_es
	 * @return Corpus ID if created successfully, null if not created.
	 */
	public String createCorpus(String tsNomCorpus, String tsLangage) {
		String lsReturn = "";
		String lsIdCorpus = "";

		lsReturn = poCfg.postRequest(poCfg.getPacteBackend() + "Corpora/corpus", "{\"title\": \"" + tsNomCorpus
				+ "\",\"description\":\""
				+ "\",\"version\":\"\",\"source\":\"\", \"addAllPermissionsOnTranscoderBucketToOwner\":true, \"reference\":\"\",\"languages\":[\""
				+ tsLangage + "\"]}", USERTYPE.CustomUser);
		if (lsReturn != null && !lsReturn.isEmpty()) {
			lsIdCorpus = poCfg.getJsonFeature(lsReturn, "id");
			if (poCfg.getVerbose())
				System.out.println("Corpus " + tsNomCorpus + " (" + lsIdCorpus + ") a été créé!");
		} else
			return null;

		return lsIdCorpus;
	}

	/**
	 * Destroy a corpus and everything contained within (documents, groups,
	 * annotations, etc).
	 * 
	 * @param tsIdCorpus
	 * @return
	 */
	public boolean deleteCorpus(String tsIdCorpus) {
		String lsReturn = "";

		if (tsIdCorpus == null || tsIdCorpus.isEmpty())
			return false;

		lsReturn = poCfg.deleteRequest(poCfg.getPacteBackend() + "Corpora/corpus/" + tsIdCorpus, USERTYPE.CustomUser,
				null);
		if (lsReturn != null && lsReturn == "") {
			return true;
		}
		return false;
	}

	public String createBucket(String tsIDCorpus, String tsNomBucket) {
		String lsReturn = "";

		// Ajouter un groupe pertinent
		// String lsIdBucket1 = UUID.randomUUID().toString();
		lsReturn = poCfg.postRequest(poCfg.getPacteBackend() + "Corpora/corpusBucket/" + tsIDCorpus,
				"{\"id\":\"\",\"name\":\"" + tsNomBucket + "\"}", USERTYPE.CustomUser);

		if (lsReturn != null && !lsReturn.isEmpty()) {
			JSONObject loJson = new JSONObject(lsReturn);
			return loJson.getString("bucketId");
		}

		return null;
	}

	public String registerSchema(String tsSchema) {
		String lsReturn = "";

		lsReturn = poCfg.postRequest(poCfg.getPacteBackend() + "Schemas/schema",
				"{\"schemaJsonContent\": \"" + tsSchema.replace("\"", "\\\"") + "\"}", USERTYPE.CustomUser);

		if (lsReturn.contains("{\"id\":\"")) {
			JSONObject loJson = new JSONObject(lsReturn);

			return loJson.getString("id");
		} else
			return null;
	}

	public boolean deleteSchema(String tsIdSchema) {

		poCfg.deleteRequest(poCfg.getPacteBackend() + "Schemas/schema/" + tsIdSchema, USERTYPE.CustomUser, null);

		return true;
	}

	/**
	 * Get schema id from name, filtered by corpus and group
	 * 
	 * @param tsSchemaName
	 * @param tsCorpusId
	 * @param tsBucketId
	 * @return
	 */
	public String getSchemaId(String tsSchemaName, String tsCorpusId, String tsBucketId) {
		String lsSchemaList = null;
		JSONArray loSchemas = null;
		String lsSchemaId = null;

		// Aller chercher tous les schémas
		lsSchemaList = poCfg.getRequest(poCfg.getPacteBackend() + "Schemas/schemas", USERTYPE.CustomUser, null);
		loSchemas = new JSONArray(lsSchemaList);

		for (int lniCpt = 0; lniCpt < loSchemas.length(); lniCpt++) {
			JSONObject loObj = loSchemas.getJSONObject(lniCpt);

			if (((String) ((JSONObject) loObj.get("schema")).get("schemaType")).equalsIgnoreCase(tsSchemaName)) {
				lsSchemaId = ((String) ((JSONObject) loObj.get("schema")).get("id"));
				JSONArray loaBuckets = loObj.getJSONArray("relatedCorpusBuckets");

				if ((tsBucketId == null || tsBucketId == "") && loaBuckets.length() == 0)
					return lsSchemaId;
				else if (tsBucketId != null && !tsBucketId.isEmpty()) {
					// Vérifier que la bucket en bien enregistrée
				}
			}
		}

		return null;
	}

	/**
	 * Enregistrer un schéma dans un groupe d'annotation à partir d'un schéma
	 * existant.
	 * 
	 * @param tsIdSchema
	 * @param tsIdCorpus
	 * @param tsIdBucket
	 * @return
	 */
	public boolean copySchemaToGroup(String tsIdSchema, String tsIdCorpus, String tsIdBucket) {

		poCfg.putRequest(poCfg.getPacteBackend() + "Schemas/schemaToCorpusBucket/" + tsIdSchema,
				"{\"corpusId\": \"" + tsIdCorpus + "\", \"bucketId\": \"" + tsIdBucket + "\"}", USERTYPE.CustomUser);

		return true;
	}

	/**
	 * Ajouter un nouveau document
	 * 
	 * @param tsCorpusId
	 * @param tsContent
	 * @param tsToken
	 * @param tsLangage
	 * @return
	 */
	public String addDocument(String tsCorpusId, String tsContent, String tsTitle, String tsSource, String tsLangage) {
		String lsReturn = "";
		String lsIdDoc = null;

		lsReturn = poCfg.postRequest(poCfg.getPacteBackend() + "RACSProxy/corpora/" + tsCorpusId + "/documents",
				"{\"title\": \"" + tsTitle + "\",\"source\": \"" + tsSource + "\",\"text\": \""
						+ tsContent.replace("\"", "\\\"") + "\",\"language\": \"" + tsLangage + "\"}",
				USERTYPE.CustomUser);

		if (lsReturn != null && !lsReturn.isEmpty())
			lsIdDoc = poCfg.getJsonFeature(lsReturn, "id");

		return lsIdDoc;
	}

	public PacteDocument getDocument(String tsCorpusID, String tsDocumentID) {
		String lsContent = null;
		String lsTitle = null;
		String lsSource = null;
		String lsLanguages = null;
		String lsReturn = "";

		lsReturn = poCfg.getRequest(
				poCfg.getPacteBackend() + "RACSProxy/corpora/" + tsCorpusID + "/documents/" + tsDocumentID,
				USERTYPE.CustomUser, null);

		if (lsReturn != null && !lsReturn.isEmpty()) {
			lsContent = new JSONObject(lsReturn).getString("text");
			lsTitle = new JSONObject(lsReturn).getString("title");
			lsSource = new JSONObject(lsReturn).getString("source");
			lsLanguages = new JSONObject(lsReturn).getString("language");

			return new PacteDocument(tsDocumentID, lsTitle, lsContent, lsSource, lsLanguages);
		}

		return null;
	}

	/**
	 * Add a new annotation to a group.
	 * 
	 * @param tsCorpusId
	 * @param tsGroupId
	 * @param tsAnnotation
	 * @return
	 */
	public String addAnnotation(String tsCorpusId, String tsGroupId, String tsAnnotation) {
		String lsAnnotId = null;
		String lsReturn = "";
		lsReturn = poCfg.postRequest(
				poCfg.getPacteBackend() + "RACSProxy/corpora/" + tsCorpusId + "/buckets/" + tsGroupId + "/annotations",
				tsAnnotation, USERTYPE.CustomUser);

		if (lsReturn != null && !lsReturn.isEmpty())
			lsAnnotId = poCfg.getJsonFeature(lsReturn, "id");

		return lsAnnotId;
	}

	/**
	 * Add new contact to the configured custom user
	 * 
	 * @param tsBucketName
	 * @param tsToken
	 * @return
	 */
	public String getBucketID(String tsBucketName, String tsCorpusId) {
		String lsReturn = "";
		List<NameValuePair> lasParam = new ArrayList<NameValuePair>();

		lasParam.add(new BasicNameValuePair("includeSchemaJson", "false"));
		lsReturn = poCfg.getRequest(poCfg.getPacteBackend() + "RACSProxy/corpora/" + tsCorpusId + "/structure",
				USERTYPE.CustomUser, lasParam);

		if (lsReturn != null && !lsReturn.isEmpty()) {
			JSONArray loRet = new JSONObject(lsReturn).getJSONArray("buckets");
			for (int lniCpt = 0; lniCpt < loRet.length(); lniCpt++) {
				if (loRet.getJSONObject(lniCpt).getString("name").equals(tsBucketName))
					return loRet.getJSONObject(lniCpt).getString("id");
			}
		}
		return null;
	}

	public boolean copyAnnotationGroup(String tsCorpusId, String tsGroupFromId, String tsGroupToId) {
		return false;
	}
}
