package ca.crim.nlp.pacte.client.transcoders;

import java.io.File;
import java.util.List;
import java.util.Map;

import ca.crim.nlp.pacte.QuickConfig;

public interface ITranscoder {

	public boolean importToPacte(QuickConfig toConfig, List<File> toaFiles, String tsCorpus, String tsGroupId, Map<String, String> toCustomeParamaters);
	
	public boolean exportFromPacte(QuickConfig toConfig, File toOutputDirectory, String tsCorpus, String tsGroupId, Map<String, String> toCustomeParamaters);
}
