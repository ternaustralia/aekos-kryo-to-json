package au.org.aekos.kryo.processor;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.ItemProcessor;

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

import au.org.aekos.core.values.AbstractValue;
import au.org.aekos.core.values.Entity;
import au.org.aekos.core.values.InvalidAekosRdfObjectException;
import au.org.aekos.core.values.Statement;
import au.org.aekos.core.values.ValueCollection;
import au.org.aekos.kryo.JsonWrapper;
import au.org.aekos.rdf.service.JenaModelSupport;

public class EntityProcessor implements ItemProcessor<Entity, JsonWrapper> {

	private final Gson gson = new Gson();
	
	@Override
	public JsonWrapper process(Entity e) throws Exception {
		EntityWrapper wrapped = EntityWrapper.newInstance(e);
		String json = gson.toJson(wrapped);
		String entityId = e.getId();
		return new JsonWrapper(json, entityId);
	}
	
	@SuppressWarnings("unused")
	private static class EntityWrapper {
		private final String id;
		private final String type;
		private final Map<String, Object> data;

		public EntityWrapper(String id, String type, Map<String, Object> data) {
			this.id = id;
			this.type = type;
			this.data = data;
		}

		public static EntityWrapper newInstance(Entity e) {
			Map<String, Object> theData = new HashMap<>();
			for (Statement curr : e.getStatements()) {
				String predicate = curr.getPredicate().getName();
				Object value = new ProxyModelSupport().extractValue(curr.getValue());
				theData.put(predicate, value);
			}
			return new EntityWrapper(e.getId(), e.getType(), theData);
		}
	}
}

class ProxyModelSupport extends JenaModelSupport {

	private Object tempValue;
	
	public Object extractValue(AbstractValue value) {
		if (value instanceof ValueCollection) {
			return handleCollection((ValueCollection)value);
		}
		handleSingleObject(value);
		return tempValue;
	}
	
	private void handleSingleObject(AbstractValue value) {
		tempValue = null;
		try {
			value.appendToRdfModel(this);
		} catch (InvalidAekosRdfObjectException e) {
			throw new RuntimeException("Failed to extract value from " + value, e);
		}
	}

	private Object handleCollection(ValueCollection value) {
		List<Object> result = new LinkedList<>();
		for (AbstractValue curr: value.getValues()) {
			handleSingleObject(curr);
			result.add(tempValue);
		}
		return result;
	}

	@Override
	public Resource resourceWithProjectNS(String instanceId, String shortName) {
		tempValue = String.format("identifier=%s", instanceId);
		return null;
	}

	@Override
	public Resource resourceReferenceWithProjectNS(String instanceId) {
		tempValue = "<" + instanceId + ">";
		return null;
	}

	@Override
	public Literal literal(String literalString) {
		tempValue = literalString;
		return null;
	}

	@Override
	public Literal literal(BigDecimal value) {
		tempValue = value.longValue();
		return null;
	}

	@Override
	public Literal literal(Boolean value) {
		tempValue = value;
		return null;
	}

	@Override
	public Literal literal(Calendar value) {
		tempValue = value.getTime().getTime();
		return null;
	}
	
}
