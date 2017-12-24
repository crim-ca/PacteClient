package ca.crim.nlp.pacte.client;

public class PacteDocument {

	private String psContent = null;
	private String psTitle = null;
	private String psID = null;
	private String psSource = null;
	private String psLanguages = null;
	
	public PacteDocument(String tsID, String tsTitle, String tsContent, String tsSource, String tsLanguages) {
		psContent = tsContent;
		psTitle = tsTitle;
		psID = tsID;
		psSource = tsSource;
		psLanguages = tsLanguages;
	}
	
	public String getContent() {
		return psContent;
	}
	
	public String getTitle() {
		return psTitle;
	}

	public String getID() {
		return psID;
	}

	public String getSource() {
		return psSource;
	}

	public String getLanguages() {
		return psLanguages;
	}
}
