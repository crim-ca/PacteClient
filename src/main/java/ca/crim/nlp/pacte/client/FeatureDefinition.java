package ca.crim.nlp.pacte.client;

import java.util.ArrayList;
import org.json.JSONObject;
import java.util.List;

public class FeatureDefinition {
	Boolean searchable = false;
	String description = "";
	List<String> searchModes = new ArrayList<String>();
	String type = "";
	String name = "";
	String defaut = null;
	boolean isRequired = false;
	// TODO: add more options for feature (min, max, default, format, etc)

	public FeatureDefinition(String tsName, String tsType, String tsDescription, String tsDefault, Boolean tbSearchable,
			List<String> toSearchModes, boolean tbRequired) {
		this.name = tsName;
		this.description = tsDescription!=null?tsDescription.trim():"";
		this.type = tsType;
		this.defaut = tsDefault;
		this.searchable = tbSearchable;
		if (toSearchModes != null)
			this.searchModes.addAll(toSearchModes);
		this.isRequired = tbRequired;
	}

	public FeatureDefinition(String tsJsonDefintion) {
		JSONObject loFeat = new JSONObject(tsJsonDefintion);
		this.name = loFeat.optString("name");
		this.description = loFeat.optString("description");
		this.type = loFeat.optString("type");
		this.defaut = loFeat.optString("default");
		this.searchable = loFeat.optBoolean("searchable");
		if (loFeat.has("seachModes"))
			this.searchModes.add(loFeat.optJSONArray("searchModes").toString());
		this.isRequired = loFeat.optBoolean("required");
	}

	/**
	 * Get the json definition for this feature
	 */
	public String toString() {
		return toJSON().toString();
	}
	
	public JSONObject toJSON() {
		JSONObject loFeat = new JSONObject();
		loFeat.put("name", this.name);
		loFeat.put("description", this.description);		
		loFeat.put("type", this.type);
		loFeat.put("default", this.defaut);
		loFeat.put("searchable", this.searchable);
		loFeat.put("searchModes", this.searchModes);
		loFeat.put("required", this.isRequired);
		return loFeat;
	}
}
