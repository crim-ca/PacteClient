package ca.crim.nlp.pacte.client;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class SchemaData {
	public Map<String, String> FeatureList = new HashMap<String, String>();
	public String SchemaType = null;
	public String TargetType = null;
	
	public SchemaData(String tsJson) {
		JSONObject loSchema = new JSONObject(tsJson);
		this.SchemaType = loSchema.get("schemaType").toString();
		this.TargetType = loSchema.get("targetType").toString();
		
		if (loSchema.has("schema"))
			loSchema = new JSONObject(loSchema.getJSONObject("schema").getString("schemaJsonContent"));

		loSchema = loSchema.getJSONObject("properties");

		for (String lsKey : loSchema.keySet()) {
			if (",schematype,_corpusid,_documentid,offsets,".indexOf("," + lsKey.toLowerCase() + ",") < 0)
				FeatureList.put(lsKey, loSchema.getString(lsKey).toString());
		}

		System.out.println(loSchema.toString());
	}
}
