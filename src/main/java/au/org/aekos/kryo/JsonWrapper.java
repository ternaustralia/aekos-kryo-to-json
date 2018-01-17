package au.org.aekos.kryo;

public class JsonWrapper {

	private final String json;
	private final String entityId;
	
	public JsonWrapper(String json, String entityId) {
		this.json = json;
		this.entityId = entityId;
	}

	public String getJson() {
		return json;
	}

	public String getEntityId() {
		return entityId;
	}
}
