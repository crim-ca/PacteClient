package ca.crim.nlp.pacte.client.transcoders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import ca.crim.nlp.pacte.QuickConfig;
import ca.crim.nlp.pacte.client.SchemaFactory;
import ca.crim.nlp.pacte.client.Corpus;
import ca.crim.nlp.pacte.client.FeatureDefinition;
import ca.crim.nlp.pacte.client.SchemaData;

public class Transcriber implements ITranscoder {

	// TODO: add hook function for CustomParamter generator
	
	@Override
	/**
	 * Trans/topics/speakers tags are document-level annotations/schemas, the rest
	 * are surface-level. Corpus and group should already exist in Pacte.
	 */
	public boolean importToPacte(QuickConfig toConfig, List<File> toaFiles, String tsCorpusId, String tsGroupId,
			Map<String, String> toCustomParamaters) {
		// Validate parameters
		if (toCustomParamaters == null)
			System.out.println("No params");

		// Read transcriber files
		for (File loFile : toaFiles) {
			TrsData loData = readTrsFile(loFile, "|-", "-|");

			// Encode the schemas and data
			List<String> lasSchemas = generateSchemas();
			for (String lsSchema : lasSchemas) {
				// TODO: check is schemas exist in the corpus+group, if not, register it
				Corpus loCorp = new Corpus(toConfig);
				loCorp.getSchemaId(new SchemaData(lsSchema).SchemaType, tsCorpusId, tsGroupId);
			}

			// Upload the data and schema

		}
		return false;
	}

	/**
	 * Create the basic Transcriber annotation schemas
	 * @return
	 */
	private List<String> generateSchemas() {
		List<String> loSchemas = new ArrayList<String>();
		
		// Topics
		// <Topic id="to1" desc="QP1"/>
		List<FeatureDefinition> loOpts = new ArrayList<FeatureDefinition>();
		loOpts.add( new FeatureDefinition("id", "String", "Topic identifier", "", false, null, true));
		loOpts.add( new FeatureDefinition("description", "String", "Topic description", "", false, null, false));
		loSchemas.add( SchemaFactory.generateDocumentSchema("Topic", "Topic", loOpts));
		
		// SPEAKERS
		// <Speaker id="spk1" name="RC" check="no" dialect="native" accent=""
		// scope="local"/>
		loOpts = new ArrayList<FeatureDefinition>();
		loOpts.add( new FeatureDefinition("id", "String", "Speaker identifier", "", false, null, true));
		loOpts.add( new FeatureDefinition("name", "String", "Speaker's name", "", false, null, false));
		loOpts.add( new FeatureDefinition("check", "String", "?", "", false, null, false));
		loOpts.add( new FeatureDefinition("dialect", "String", "Speaker's name", "", false, null, false));
		loOpts.add( new FeatureDefinition("accent", "String", "Speaker's name", "", false, null, false));
		loOpts.add( new FeatureDefinition("scope", "String", "Speaker's name", "", false, null, false));
		loSchemas.add(SchemaFactory.generateDocumentSchema("Speaker", "Speaker", loOpts));
	
		// <Section type="report" startTime="0" endTime="13.942" topic="to1">
		loOpts = new ArrayList<FeatureDefinition>();
		loOpts.add( new FeatureDefinition("type", "String", "Section type", "", false, null, true));
		loOpts.add( new FeatureDefinition("startTime", "String", "Start time of the section", "", false, null, true));
		loOpts.add( new FeatureDefinition("endTime", "String", "End time of the section", "", false, null, true));
		loOpts.add( new FeatureDefinition("topic", "String", "Topic reference", "", false, null, false));
		loSchemas.add(SchemaFactory.generateSurfaceSchema("Section", "Section", loOpts));
		
		// <Turn speaker="spk1" startTime="3906.731" endTime="3916.829">
		loOpts = new ArrayList<FeatureDefinition>();
		loOpts.add( new FeatureDefinition("speaker", "String", "Speaker identifier for this turn", "", false, null, true));
		loOpts.add( new FeatureDefinition("startTime", "String", "Start time of the turn", "", false, null, true));
		loOpts.add( new FeatureDefinition("endTime", "String", "End time of the turn", "", false, null, true));
		loSchemas.add(SchemaFactory.generateSurfaceSchema("Turn", "Turn", loOpts));
		
		// <Sync time="3910.332"/>
		loOpts = new ArrayList<FeatureDefinition>();
		loOpts.add( new FeatureDefinition("time", "String", "Time of this entry", "", false, null, true));
		loSchemas.add(SchemaFactory.generateSurfaceSchema("Sync", "Sync", loOpts));
		
		// <Event desc="pi" type="pronounce" extent="instantaneous"/>
		loOpts = new ArrayList<FeatureDefinition>();
		loOpts.add( new FeatureDefinition("desc", "String", "Event description", "", false, null, false));
		loOpts.add( new FeatureDefinition("type", "String", "Type of event", "", false, null, false));
		loOpts.add( new FeatureDefinition("extent", "String", "?", "", false, null, false));
		loSchemas.add(SchemaFactory.generateSurfaceSchema("Event", "Event", loOpts));
		
		return loSchemas;
	}
	
	private TrsData readTrsFile(File toFile, String tsCustomBeginSep, String tsCustomEndSep) {
		TrsData loData = new TrsData();
		StringBuilder loContent = new StringBuilder();

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(toFile);

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			// TRANS
			NodeList laoTrans = doc.getElementsByTagName("Trans");
			NamedNodeMap loAttr = ((Element) laoTrans.item(0)).getAttributes();
			for (int lniCpt = 0; lniCpt < loAttr.getLength(); lniCpt++)
				loData.tasTrans.put(loAttr.item(lniCpt).getNodeName(), loAttr.item(lniCpt).getNodeValue());

			// TOPICS
			// <Topic id="to1" desc="QP1"/>
			NodeList laoTopics = doc.getElementsByTagName("Topic");
			for (int lniCpt = 0; lniCpt < laoTopics.getLength(); lniCpt++) {
				NamedNodeMap loAtts = laoTopics.item(lniCpt).getAttributes();
				String lsKey = loAtts.getNamedItem("id").getNodeValue();
				loData.tasTopics.put(lsKey, new HashMap<String, String>());
				// loop all except id
				for (int lniCptAtt = 0; lniCptAtt < loAtts.getLength(); lniCptAtt++) {
					if (!loAtts.item(lniCptAtt).getNodeName().equals("id"))
						loData.tasTopics.get(lsKey).put(loAtts.item(lniCptAtt).getNodeName(),
								loAtts.item(lniCptAtt).getNodeValue());
				}
			}

			// SPEAKERS
			// <Speaker id="spk1" name="RC" check="no" dialect="native" accent=""
			// scope="local"/>
			NodeList laoSpeakers = doc.getElementsByTagName("Speaker");
			for (int lniCpt = 0; lniCpt < laoSpeakers.getLength(); lniCpt++) {
				NamedNodeMap loAtts = laoSpeakers.item(lniCpt).getAttributes();
				String lsKey = loAtts.getNamedItem("id").getNodeValue();
				loData.tasSpeakers.put(lsKey, new HashMap<String, String>());
				// loop all except id
				for (int lniCptAtt = 0; lniCptAtt < loAtts.getLength(); lniCptAtt++) {
					if (!loAtts.item(lniCptAtt).getNodeName().equals("id"))
						loData.tasSpeakers.get(lsKey).put(loAtts.item(lniCptAtt).getNodeName(),
								loAtts.item(lniCptAtt).getNodeValue());
				}
			}

			// Section -> turn -> sync
			NodeList laoSection = doc.getElementsByTagName("Section");
			for (int lniCptSec = 0; lniCptSec < laoSection.getLength(); lniCptSec++) {
				// Turns
				NodeList laoTurns = laoSection.item(lniCptSec).getChildNodes();
				for (int lniCptTurn = 0; lniCptTurn < laoSection.getLength(); lniCptTurn++) {

					// Process line-byline
					List<String> lasLines = Arrays.asList(laoTurns.item(lniCptTurn).getTextContent().split("\n\r|\n"));
					for (String lsLine : lasLines) {
						if (lsLine.trim().toLowerCase().startsWith("<sync")) {

						} else {
							// Text content, encode manual annotation if separator present
						}
					}
					// Ajouter le turn avec la position finale du texte
				}

				// Ajouter la section avec la position finale du texte

			}

			// loData.tasTrans.put(loAttr.item(lniCpt).getNodeName(),
			// loAttr.item(lniCpt).getNodeValue());

			// System.out.println("----------------------------");

			// for (int temp = 0; temp < nList.getLength(); temp++) {
			//
			// Node nNode = nList.item(temp);
			//
			// System.out.println("\nCurrent Element :" + nNode.getNodeName());
			//
			// Element eElement = (Element) nNode;
			//
			// NamedNodeMap loAttr = eElement.getAttributes();
			// for (int lniCpt = 0; lniCpt < loAttr.getLength(); lniCpt++) {
			// System.out.println(
			// loAttr.item(lniCpt).getNodeName() + " : " +
			// loAttr.item(lniCpt).getNodeValue());
			//
			// }
			// }

		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		loData.lsContent = loContent.toString();

		return loData;
	}

	@Override
	public boolean exportFromPacte(QuickConfig toConfig, File toOutputDirectory, String tsCorpus, String tsGroupId,
			Map<String, String> toCustomeParamaters) {
		// TODO Auto-generated method stub
		return false;
	}

	private class TrsData {
		Map<String, Map<String, String>> tasSpeakers = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> tasTopics = new HashMap<String, Map<String, String>>();
		Map<String, String> tasTrans = new HashMap<String, String>();
		String lsContent = null;
		Map<String, Map<String, String>> taoSections = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> taoTurn = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> taoSync = new HashMap<String, Map<String, String>>();
	}
}
