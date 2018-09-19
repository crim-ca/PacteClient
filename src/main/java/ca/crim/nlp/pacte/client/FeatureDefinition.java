package ca.crim.nlp.pacte.client;

import java.util.List;

public class FeatureDefinition {
	Boolean searchable = false;
	String description = "";
	List<String> searchModes = null;
	String type = "";
	String name = "";
	String defaut = null;
	boolean isRequired = false;
	// TODO: add more options for feature (min, max, default, format, etc)

	public FeatureDefinition(String tsName, String tsType, String tsDescription, String tsDefault, Boolean tbSearchable,
			List<String> tsSearchModes, boolean tbRequired) {
		this.name = tsName;
		this.description = tsDescription;
		this.type = tsType;
		this.defaut = tsDefault;
		this.searchable = tbSearchable;
		this.searchModes = tsSearchModes;
		this.isRequired = tbRequired;
	}

	/**
	 * Get json definition for this feature
	 */
	public String toString() {
		return "{\"name\":\"" + name + "\"," + "\"type\":\"" + type + "\"" + "\"description\":\"" + description + "\","
				+ "\"searchable\":\"" + searchable.toString() + "\"," + "\"searchModes\": [" + "]," + "}";
	}
}
