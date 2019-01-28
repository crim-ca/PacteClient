package ca.crim.nlp.pacte.client;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class SchemaData {
	public enum TARGET {
		document_surface1d, document, corpus
	};

	public Map<String, FeatureDefinition> FeatureList = new HashMap<String, FeatureDefinition>();
	public String SchemaType = null;
	public TARGET TargetType = null;

	/**
	 * 
	 * @param toTarget
	 * @param tsSchemaType
	 * @param toFeats
	 */
	public SchemaData(SchemaData.TARGET toTarget, String tsSchemaType, List<FeatureDefinition> toFeats) {
		this.TargetType = toTarget;
		this.SchemaType = tsSchemaType;
		for (FeatureDefinition loF : toFeats)
			this.FeatureList.put(loF.name, loF);
	}

	/**
	 * Create schema from JSON definition
	 * 
	 * @param tsJson
	 */
	public SchemaData(String tsJson) {
		JSONObject loSchema = new JSONObject(tsJson);
		this.SchemaType = loSchema.get("schemaType").toString();
		this.TargetType = TARGET.valueOf(loSchema.get("targetType").toString());

		// If the schema comes from racs backend
		if (loSchema.has("schema"))
			loSchema = new JSONObject(loSchema.getJSONObject("schema").getString("schemaJsonContent"));

		loSchema = loSchema.getJSONObject("properties");

		for (String lsKey : loSchema.keySet()) {
			if (",schematype,_corpusid,_documentid,offsets,".indexOf("," + lsKey.toLowerCase() + ",") < 0)
				FeatureList.put(lsKey, new FeatureDefinition(loSchema.getJSONObject(lsKey).toString()));
		}
	}

	public String toString() {
		URL url = null;
		String lsSchema = "";
		StringBuilder lsFeatures = new StringBuilder();
		String lsRequired = "";

		// Choose the right template for this schema
		try {
			if (this.TargetType.equals(TARGET.document_surface1d))
				url = Resources.getResource("ca/crim/nlp/pacte/client/surface1d_schema.json");
			else if (this.TargetType.equals(TARGET.document))
				url = Resources.getResource("ca/crim/nlp/pacte/client/document_schema.json");
			else if (this.TargetType.equals(TARGET.corpus))
				url = Resources.getResource("ca/crim/nlp/pacte/client/corpus_schema.json");

			lsSchema = Resources.toString(url, Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Change name
		lsSchema = lsSchema.replace("###TEMPLATETYPE###", this.SchemaType);
		lsSchema = lsSchema.replace("###TEMPLATETITLE###", this.SchemaType);

		// Generate features
		for (String lsOpt : this.FeatureList.keySet()) {
			lsFeatures.append(", \"" + lsOpt + "\":" + this.FeatureList.get(lsOpt).toString());
			if (this.FeatureList.get(lsOpt).isRequired)
				lsRequired += ",\"" + this.FeatureList.get(lsOpt).type + "\"";
		}

		// Insert features at the end of the schema
		if ((this.FeatureList == null) || this.FeatureList.isEmpty()) {
			lsSchema = lsSchema.replace("###CUSTOMFEATURESLIST###", "");
			lsSchema = lsSchema.replace("###CUSTOMREQUIREDFIELDS###", "");

		} else if (lsFeatures.length() > 0) {
			lsSchema = lsSchema.replace("###CUSTOMFEATURESLIST###", lsFeatures.toString());
			if (lsRequired != "")
				lsSchema = lsSchema.replace("###CUSTOMREQUIREDFIELDS###", lsRequired);
			else
				lsSchema = lsSchema.replace("###CUSTOMREQUIREDFIELDS###", "");

		}

		return lsSchema;
	}
}
