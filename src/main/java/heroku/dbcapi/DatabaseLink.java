package heroku.dbcapi;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DatabaseLink {

	@JsonProperty(value="Id")
	String id;
	@JsonProperty(value="Name")
	String name;
	@JsonProperty(value="instance__c")
	String instance;
	@JsonProperty(value="token__c")
	String token;
	@JsonProperty(value="username__c")
	String username;

	@JsonProperty(value="developer__c")
	String developerId;

	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInstance() {
		return instance;
	}
	public void setInstance(String instance) {
		this.instance = instance;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getDeveloperId() {
		return developerId;
	}
	public void setDeveloperId(String developerId) {
		this.developerId = developerId;
	}
	
}
