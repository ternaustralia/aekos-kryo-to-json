package au.org.aekos.kryo.processor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.junit.Test;
import org.springframework.util.StreamUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import au.org.aekos.core.values.BooleanValue;
import au.org.aekos.core.values.DateValue;
import au.org.aekos.core.values.Entity;
import au.org.aekos.core.values.EntityReferenceIdentifier;
import au.org.aekos.core.values.Identifier;
import au.org.aekos.core.values.NumberValue;
import au.org.aekos.core.values.Predicate;
import au.org.aekos.core.values.Statement;
import au.org.aekos.core.values.TextValue;
import au.org.aekos.core.values.ValueCollection;
import au.org.aekos.kryo.JsonWrapper;

public class EntityProcessorTest {

	/**
	 * Can we serialise an Entity without any statements?
	 */
	@Test
	public void testProcess01() throws Throwable {
		EntityProcessor objectUnderTest = new EntityProcessor();
		Entity e = new Entity(SomeType.class, new EntityReferenceIdentifier("au.org.aekos.kryo.processor.SomeType:T1234"));
		JsonWrapper result = objectUnderTest.process(e);
		assertThat(result.getEntityId(), is("au.org.aekos.kryo.processor.SomeType:T1234"));
		assertJsonEquals(result.getJson(), "{\"id\":\"au.org.aekos.kryo.processor.SomeType:T1234\",\"type\":\"au.org.aekos.kryo.processor.SomeType\",\"data\":{}}");
	}
	
	/**
	 * Can we serialise an Entity with some primitive statements?
	 */
	@Test
	public void testProcess02() throws Throwable {
		EntityProcessor objectUnderTest = new EntityProcessor();
		Entity e = new Entity(SomeType.class, new EntityReferenceIdentifier("au.org.aekos.kryo.processor.SomeType:T1234"));
		e.addStatement(new Statement(new Predicate("Text", "foo"), new TextValue("bar")));
		e.addStatement(new Statement(new Predicate("Boolean", "isFoo"), new BooleanValue(true)));
		e.addStatement(new Statement(new Predicate("Number", "fooCount"), new NumberValue(33)));
		e.addStatement(new Statement(new Predicate("Date", "fooStart"), new DateValue(new Date(1516148847034l))));
		JsonWrapper result = objectUnderTest.process(e);
		assertJsonEquals(result.getJson(), getExpected("EntityProcessorTest_testProcess02_expected.json"));
	}
	
	/**
	 * Can we serialise an Entity with a statement that references other objects?
	 */
	@Test
	public void testProcess03() throws Throwable {
		EntityProcessor objectUnderTest = new EntityProcessor();
		Entity e = new Entity(SomeType.class, new EntityReferenceIdentifier("au.org.aekos.kryo.processor.SomeType:T1234"));
		e.addStatement(new Statement(new Predicate("Other", "parent"), new EntityReferenceIdentifier("au.org.aekos.OTHER:T222")));
		JsonWrapper result = objectUnderTest.process(e);
		assertJsonEquals(result.getJson(), getExpected("EntityProcessorTest_testProcess03_expected.json")); // FIXME doesn't include full package for class
	}
	
	/**
	 * Can we serialise an Entity with a statement that is a collection?
	 */
	@Test
	public void testProcess04() throws Throwable {
		EntityProcessor objectUnderTest = new EntityProcessor();
		Entity e = new Entity(SomeType.class, new EntityReferenceIdentifier("au.org.aekos.kryo.processor.SomeType:T1234"));
		ValueCollection values = new ValueCollection();
		values.getValues().add(new TextValue("name1"));
		values.getValues().add(new TextValue("name2"));
		values.getValues().add(new TextValue("name3"));
		e.addStatement(new Statement(new Predicate("Text", "names"), values));
		JsonWrapper result = objectUnderTest.process(e);
		assertJsonEquals(result.getJson(), getExpected("EntityProcessorTest_testProcess04_expected.json"));
	}
	
	/**
	 * Can we serialise an Entity with a statement that contains an indentifier?
	 */
	@Test
	public void testProcess05() throws Throwable {
		EntityProcessor objectUnderTest = new EntityProcessor();
		Entity e = new Entity(SomeType.class, new EntityReferenceIdentifier("au.org.aekos.kryo.processor.SomeType:T1234"));
		e.addStatement(new Statement(new Predicate("Text", "the_identifier"), new Identifier(SomeIdentifier.class, "au.org.aekos.OtherEntity")));
		JsonWrapper result = objectUnderTest.process(e);
		assertJsonEquals(result.getJson(), getExpected("EntityProcessorTest_testProcess05_expected.json"));
	}

	private void assertJsonEquals(String actual, String expected) {
		JsonParser p = new JsonParser();
		JsonElement parsedActual = p.parse(actual);
		JsonElement parsedExpected = p.parse(expected);
		assertEquals(parsedExpected, parsedActual);
	}

	private String getExpected(String fileName) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/kryo/" + fileName);
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(is, out);
		String json = out.toString();
		return json.replaceAll("\\s", ""); // FIXME won't work if we have spaces *inside* values
	}
}

class SomeType {}
class SomeIdentifier {}