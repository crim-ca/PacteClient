package ca.crim.nlp.pacte.client;

import java.util.List;

public class SchemaFactory {

	private static String generateSchema(String tsTemplate, String tsSchemaType, String tsSchemaName, List<FeatureDefinition> toFeatures) {
		String lsSchema = tsTemplate;
		StringBuilder lsFeatures = new StringBuilder();
		String lsRequired = "";

		// TODO: Read schema from resource

		// Change name
		lsSchema.replace("###TEMPLATETYPE###", tsSchemaType);
		lsSchema.replace("###TEMPLATETITLE###", tsSchemaName);

		// Generate features
		for (FeatureDefinition loOpt : toFeatures) {
			lsFeatures.append(loOpt.toString());
			if (loOpt.isRequired)
				lsRequired += "," + loOpt.type;
		}

		// Insert features at the end of the schema
		if ((lsFeatures != null) && lsFeatures.length() > 0)
			lsSchema = lsSchema.substring(0, lsSchema.length() - 2) + lsFeatures.toString() + "}}";
		if ((lsRequired != null) && !lsRequired.isEmpty()) {
			// FIXME: add required fields
		}

		return lsSchema;
	}

	/**
	 * Generate a surface level schema
	 * 
	 * @param tsSchemaType
	 * @param tsSchemaName
	 * @param toFeatures
	 * @return
	 */
	public static String generateSurfaceSchema(String tsSchemaType, String tsSchemaName,
			List<FeatureDefinition> toFeatures) {
		// InputStream loSch =
		// getClassLoader().getResourceAsStream("surface1d_schema.json");
		String lsTemplate = ""; // FIXME :get it
		String lsSchema = generateSchema(lsTemplate, tsSchemaType, tsSchemaName, toFeatures);

		return lsSchema;
	}

	/**
	 * Generate a document level schema
	 * 
	 * @param tsSchemaType
	 * @param tsSchemaName
	 * @param toFeatures
	 * @return
	 */
	public static String generateDocumentSchema(String tsSchemaType, String tsSchemaName,
			List<FeatureDefinition> toFeatures) {
		// InputStream loSch =
		// getClassLoader().getResourceAsStream("document_schema.json");
		String lsTemplate = ""; // FIXME :get it
		String lsSchema = generateSchema(lsTemplate, tsSchemaType, tsSchemaName, toFeatures);

		return lsSchema;
	}
}
