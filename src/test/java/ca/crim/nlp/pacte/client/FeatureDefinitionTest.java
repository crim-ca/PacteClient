package ca.crim.nlp.pacte.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FeatureDefinitionTest {

	/*
	 * Check if the tosjon and tostring function are consistant
	 */
	@Test
	public void ToJsonToStringTest() {
		FeatureDefinition loFD1 = new FeatureDefinition( "name", "type", "description", "", true, null, false);
		FeatureDefinition loFD2 = new FeatureDefinition(loFD1.toString());
		
		assertEquals(loFD1.toString().trim(), loFD2.toString().trim());
	}
	
}
