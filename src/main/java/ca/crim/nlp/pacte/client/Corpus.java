package ca.crim.nlp.pacte.client;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
	 * Import a corpus from exported files
	 * 
	 * @param tsCorpusPath
	 *            Path to the corpus exported with the {@link exportToDisk} function
	 * @return The new corpus unique identification
	 */
	public String importCorpus(String tsCorpusPath) {
		String lsReturn = null;
		String lsCorpusNewId = null;
		Map<String, String> laoGroups = new HashMap<String, String>();
		String lsCorpusOldId = null;
		String lsLang = "";

		if (!new File(tsCorpusPath).exists())
			return null;

		// Lire les métadata et recréer le corpus
		lsReturn = readFile(new File(tsCorpusPath, "corpus.json").getAbsolutePath());
		if (lsReturn == null)
			return null;
		JSONObject loCorpMeta = new JSONObject(lsReturn);
		lsCorpusOldId = loCorpMeta.getString("id");
		for (int lniCpt = 0; lniCpt < loCorpMeta.getJSONArray("languages").length(); lniCpt++)
			lsLang += loCorpMeta.getJSONArray("languages").get(lniCpt) + ",";
		lsCorpusNewId = createCorpus(loCorpMeta.getString("title") + " - Import",
				lsLang.substring(0, lsLang.length() - 1));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
		}

		// Recréer les groupes et ajouter les schémas
		lsReturn = readFile(new File(tsCorpusPath, "corpusStructure.json").getAbsolutePath());
		JSONArray lasGroups = new JSONObject(lsReturn).getJSONArray("buckets");

		for (int lniCpt = 0; lniCpt < lasGroups.length(); lniCpt++) {
			String lsGroupName = ((JSONObject) lasGroups.get(lniCpt)).getString("name");
			String lsOldGroupId = ((JSONObject) lasGroups.get(lniCpt)).getString("id");
			String lsGroupId = getGroupId(lsGroupName, lsCorpusNewId);
			if (lsGroupId == null)
				lsGroupId = createBucket(lsCorpusNewId, lsGroupName);
			laoGroups.put(lsOldGroupId, lsGroupId);

			// Ajouter les schémas disponibles
			JSONArray lasSchemas = ((JSONObject) lasGroups.get(lniCpt)).getJSONArray("schemas");
			for (int lniCptSchema = 0; lniCptSchema < lasSchemas.length(); lniCptSchema++) {
				String lsSchema = null;
				File loFile = new File(tsCorpusPath, "groups/" + lsOldGroupId + "/"
						+ ((JSONObject) lasSchemas.get(lniCptSchema)).getString("schemaType") + ".schema");
				if (loFile.exists())
					lsSchema = new JSONObject(readFile(loFile.getAbsolutePath())).getJSONObject("schema")
							.getString("schemaJsonContent");
				else if (loFile.getName().equalsIgnoreCase("document_meta.schema"))
					try {
						lsSchema = new String(
								Files.readAllBytes(Paths.get(ClassLoader.class
										.getResource("/ca/crim/nlp/pacte/client/document_meta.json").toURI())),
								Charset.forName("UTF-8"));
					} catch (IOException | URISyntaxException e) {
						e.printStackTrace();
					}

				String lsSchemaId = getSchemaId(new JSONObject(lsSchema).getString("schemaType"), "", "");
				if (lsSchemaId == null)
					lsSchemaId = registerSchema(lsSchema);
				copySchemaToGroup(lsSchemaId, lsCorpusNewId, lsGroupId);
			}
		}

		/*
		 * Uploader les documents et créer la table de correspondance pour les
		 * identifiants
		 */
		try (DirectoryStream<Path> directoryStream = Files
				.newDirectoryStream(Paths.get(new File(tsCorpusPath, "documents").getAbsolutePath()))) {
			for (Path path : directoryStream) {
				String lsDocEx = readFile(path.toAbsolutePath().toString());
				JSONObject loDoc = new JSONObject(lsDocEx);
				String lsDocOldId = loDoc.getString("id");
				String lsDocId = addDocument(lsCorpusNewId, loDoc.getString("text"), loDoc.getString("title"),
						loDoc.getString("source"), loDoc.getString("language"));
				// Ajouter les annotations
				for (String lsGroup : laoGroups.keySet()) {
					File loAnnotFile = new File(tsCorpusPath, "groups/" + lsGroup + "/" + lsDocOldId + ".json");
					if (!loAnnotFile.exists())
						continue;
					String lsAnnot = readFile(loAnnotFile.getAbsolutePath());
					JSONObject loAnnots = new JSONObject(lsAnnot);
					if (loAnnots.isNull(lsCorpusOldId))
						continue;
					loAnnots = loAnnots.getJSONObject(lsCorpusOldId).getJSONObject(lsGroup);

					for (int lniCpt = 0; lniCpt < loAnnots.names().length(); lniCpt++) {
						JSONArray loAnnotations = loAnnots.getJSONArray(loAnnots.names().get(lniCpt).toString());
						for (int lniCptAnn = 0; lniCptAnn < loAnnotations.length(); lniCptAnn++) {
							JSONObject loAnn = loAnnotations.getJSONObject(lniCptAnn);
							loAnn.remove("annotationId");
							loAnn.remove("_corpusID");
							loAnn.put("_corpusID", lsCorpusNewId);
							loAnn.remove("_documentID");
							loAnn.put("_documentID", lsDocId);
							addAnnotation(lsCorpusNewId, laoGroups.get(lsGroup), loAnn.toString());
						}
					}
				}
			}
		} catch (IOException ex) {
		}

		return null;
	}

	/**
	 * Save a corpus' documents, groups and annotations to disk in subfolders. Will
	 * not retain user rights. For large corpus, please use the batch functionality
	 * of the back-end.
	 * 
	 * @param tsCorpusId
	 *            Corpus unique id to export.
	 * @param tsOuputPath
	 *            The local directory to store the exported corpus.
	 * @param tsExportGroupId
	 *            : Ids of accessible groups to export. If none listed, all
	 *            accessible groups are exported.
	 * @return True if exported with success, false if error during export.
	 */
	public boolean exportToDisk(String tsCorpusId, String tsOutputPath, List<String> tasExportGroupId) {
		String lsReturn = "";
		Map<String, List<String>> lasBuckets = new HashMap<String, List<String>>();
		File loDocsFolder = null;
		File loGroupsFolder = null;

		// Prepare the subfolders
		if (!(new File(tsOutputPath)).exists())
			return false;

		loDocsFolder = new File(tsOutputPath, "documents");
		loDocsFolder.mkdirs();
		loGroupsFolder = new File(tsOutputPath, "groups");
		loGroupsFolder.mkdirs();

		// Download corpus specs and save them
		writeFile(getCorpusMetadata(tsCorpusId), "corpus.json", tsOutputPath);

		// Download the corpus structure and replicate it
		lsReturn = poCfg.getRequest(poCfg.getPacteBackend() + "RACSProxy/corpora/" + tsCorpusId + "/structure",
				USERTYPE.CustomUser, null);
		if (lsReturn != null && !lsReturn.isEmpty()) {
			// Save to keep track or group names and schemas
			writeFile(lsReturn, "CorpusStructure.json", tsOutputPath);

			JSONObject loRet = new JSONObject(lsReturn);
			for (int lniCpt = 0; lniCpt < loRet.getJSONArray("buckets").length(); lniCpt++) {
				String lsId = ((JSONObject) loRet.getJSONArray("buckets").get(lniCpt)).getString("id");

				if ((tasExportGroupId == null) || tasExportGroupId.isEmpty() || tasExportGroupId.contains(lsId)) {
					new File(loGroupsFolder, lsId).mkdirs();

					lasBuckets.put(lsId, new ArrayList<String>());
					JSONArray loSchemas = ((JSONObject) loRet.getJSONArray("buckets").get(lniCpt))
							.getJSONArray("schemas");
					for (int lniCptSchema = 0; lniCptSchema < loSchemas.length(); lniCptSchema++) {
						String lsName = ((JSONObject) loSchemas.get(lniCptSchema)).getString("schemaType");
						lasBuckets.get(lsId).add(((JSONObject) loSchemas.get(lniCptSchema)).getString("schemaType"));
						String lsSchemaId = getSchemaId(lsName, tsCorpusId, lsId);
						if (lsSchemaId != null)
							writeFile(getSchema(lsSchemaId), lsName + ".schema",
									new File(loGroupsFolder, lsId).getAbsolutePath());
					}
				}
			}
		}

		// Check if all required groups are in the structure, if not, exit.
		if (tasExportGroupId != null && !tasExportGroupId.isEmpty())
			for (String lsId : tasExportGroupId)
				if (!lasBuckets.keySet().contains(lsId)) {
					System.err.println("Missing group : " + lsId);
					return false;
				}

		// List documents
		List<PacteDocument> loDocs = getDocuments(tsCorpusId);
		// and download them
		for (PacteDocument loDoc : loDocs) {
			writeFile(poCfg.getRequest(
					poCfg.getPacteBackend() + "RACSProxy/corpora/" + tsCorpusId + "/documents/" + loDoc.getID(),
					USERTYPE.CustomUser, null), loDoc.getID() + ".json", loDocsFolder.getAbsolutePath());

			// List annotations per group and store them
			for (String lsGroupId : lasBuckets.keySet()) {
				String lsSchemas = "";
				for (String lsType : lasBuckets.get(lsGroupId))
					lsSchemas += lsGroupId + ":" + lsType + ",";
				// Get each docs/groups annotation
				writeFile(
						getAnnotations(tsCorpusId, loDoc.getID(),
								lsSchemas.isEmpty() ? "" : lsSchemas.substring(0, lsSchemas.length() - 1)),
						loDoc.getID() + ".json",
						new File(loGroupsFolder.getAbsolutePath(), lsGroupId).getAbsolutePath());
			}
		}
		// Save schema
		return false;
	}

	/**
	 * Get the definition of a corpus
	 * 
	 * @param tsCorpusId
	 *            Unique identification of the targeted corpus
	 * @return Json definition, null if not found.
	 */
	public String getCorpusMetadata(String tsCorpusId) {
		return poCfg.getRequest(poCfg.getPacteBackend() + "Corpora/corpus/" + tsCorpusId, USERTYPE.CustomUser, null);
	}

	/**
	 * Return corpus unique identification from the name. In case there are several
	 * corpora with the same name, the first is returned.
	 * 
	 * @param tsNomCorpus
	 * @return
	 */
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
	 * 
	 * @param tsCorpusId
	 * @return
	 */
	public List<PacteDocument> getDocuments(String tsCorpusId) {
		String lsResponse = null;
		List<NameValuePair> loValues = new ArrayList<NameValuePair>();
		List<PacteDocument> loDocs = new ArrayList<PacteDocument>();
		Integer lniMaxDoc = Integer.MAX_VALUE;
		Integer lniCptPage = 0;

		if (tsCorpusId == null || tsCorpusId.trim().isEmpty())
			return null;

		loValues.add(new BasicNameValuePair("entriesperpage", "2"));
		loValues.add(new BasicNameValuePair("page", lniCptPage.toString()));

		while (loDocs.size() < lniMaxDoc) {
			lsResponse = null;

			// Aller chercher la prochaine page
			loValues.remove(loValues.size() - 1);
			loValues.add(new BasicNameValuePair("page", (++lniCptPage).toString()));

			lsResponse = poCfg.getRequest(poCfg.getPacteBackend() + "Corpora/documentsCorpus/" + tsCorpusId,
					USERTYPE.CustomUser, loValues);
			// System.out.println(lsResponse);

			if (lsResponse == null || lsResponse.contains("documents\":[]"))
				return loDocs;
			lniMaxDoc = new JSONObject(lsResponse).getInt("documentCount");

			JSONArray loJson = new JSONObject(lsResponse).getJSONArray("documents");
			for (int lniCpt = 0; lniCpt < loJson.length(); lniCpt++) {
				JSONObject loDoc = (JSONObject) loJson.get(lniCpt);
				loDocs.add(new PacteDocument(loDoc.getString("id"), loDoc.getString("title"), null, null,
						loDoc.getString("language"), loDoc.getLong("docByteSize"), loDoc.getString("dateAdded"),
						loDoc.getString("path")));
			}
		}

		return loDocs;
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
	 * 
	 * @param tsSchemaId
	 * @return
	 */
	public String getSchema(String tsSchemaId) {
		String lsSchema = null;

		// Aller chercher le schéma
		lsSchema = poCfg.getRequest(poCfg.getPacteBackend() + "Schemas/schema/" + tsSchemaId, USERTYPE.CustomUser,
				null);

		if (lsSchema == null || lsSchema.isEmpty())
			return null;
		else
			return lsSchema;
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
				JSONArray loaCorpus = loObj.getJSONArray("relatedCorpusBuckets");

				// Schema pas dans un groupe
				if ((tsBucketId == null || tsBucketId == "") && (tsCorpusId == null || tsCorpusId == "")
						&& loaCorpus.length() == 0)
					return lsSchemaId;
				else if (((tsBucketId != null && !tsBucketId.isEmpty())
						|| (tsCorpusId != null || !tsCorpusId.isEmpty())) && loaCorpus.length() > 0) {
					// Vérifier que la bucket en bien enregistrée
					String lsCorp = ((JSONObject) loaCorpus.get(0)).getString("corpusId");
					String lsBuck = ((JSONObject) loaCorpus.get(0)).getString("bucketId");

					if (lsCorp.isEmpty() ? true
							: lsCorp.equals(tsCorpusId) && lsBuck.isEmpty() ? true : lsBuck.equals(tsBucketId))
						return lsSchemaId;
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

			return new PacteDocument(tsDocumentID, lsTitle, lsContent, lsSource, lsLanguages, null, null, null);
		}

		return null;
	}

	/**
	 * Get the number of documents in the corpus
	 * 
	 * @param tsCorpusId
	 * @return
	 */
	public Integer getSize(String tsCorpusId) {
		String lsResponse = null;
		lsResponse = poCfg.getRequest(poCfg.getPacteBackend() + "RACSProxy/corpora/" + tsCorpusId + "/structure",
				USERTYPE.CustomUser, null);

		if (lsResponse != null && !lsResponse.isEmpty()) {
			JSONObject loJson = new JSONObject(lsResponse);
			if (loJson.getString("") != null)
				return loJson.getInt("");
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

		if (lsReturn != null && !lsReturn.isEmpty() && !lsReturn.contains("Not Found:"))
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
	public String getGroupId(String tsBucketName, String tsCorpusId) {
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

	public String getAnnotations(String tsCorpusId, String tsDocId, String tsSchemaTypes) {
		String lsReturn = "";
		List<NameValuePair> lasParam = new ArrayList<NameValuePair>();

		lasParam.add(new BasicNameValuePair("schemaTypes", tsSchemaTypes));

		lsReturn = poCfg.getRequest(
				poCfg.getPacteBackend() + "RACSProxy/annosearch/corpora/" + tsCorpusId + "/documents/" + tsDocId,
				USERTYPE.CustomUser, lasParam);

		if (lsReturn != null && !lsReturn.isEmpty()) {
			return lsReturn;
		}
		return null;
	}

	public boolean copyAnnotationGroup(String tsCorpusId, String tsGroupFromId, String tsGroupToId) {
		return false;
	}

	private boolean writeFile(String tsContent, String tsFileName, String tsPath) {
		try {
			Files.write((new File(tsPath, tsFileName)).toPath(), Arrays.asList(tsContent.split("\r\n")),
					Charset.forName("UTF-8"));

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private String readFile(String path) {
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			return null;
		}
		return new String(encoded, Charset.forName("UTF-8"));
	}
}
