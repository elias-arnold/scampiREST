package de.scampiRest.data;

public class ScampiService {

	private String name;
	private String version; 
	private String description;
	private Integer messages;

	public ScampiService() {		
	}

	public ScampiService(String name, String version, String description, Integer messages) {
		super();
		this.name = name;
		this.version = version;
		this.description = description;
		this.messages = messages;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getMessages() {
		return messages;
	}

	public void setMessages(Integer messages) {
		this.messages = messages;
	}

	
}
