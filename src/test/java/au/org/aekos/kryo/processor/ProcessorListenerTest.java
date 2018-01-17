package au.org.aekos.kryo.processor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import org.junit.Test;

public class ProcessorListenerTest {

	/**
	 * Can we could how many times processed was called?
	 */
	@Test
	public void testGetProcessedCounter01() {
		ProcessorListener objectUnderTest = new ProcessorListener();
		setValue(objectUnderTest, "processedLogThreshold", 10);
		objectUnderTest.afterProcess(null, null);
		objectUnderTest.afterProcess(null, null);
		int result = objectUnderTest.getProcessedCounter();
		assertThat(result, is(2));
	}
	
	/**
	 * Test the progress message.
	 */
	@Test
	public void testGetProcesseunter01() {
		ProcessorListener objectUnderTest = new ProcessorListener();
		
		setValue(objectUnderTest, "isStartRecorded", true);
		setValue(objectUnderTest, "startMs", 1516163067016l);
		setValue(objectUnderTest, "processedLogThreshold", 1);
		setValue(objectUnderTest, "isTotalCounted", true);
		setValue(objectUnderTest, "totalRecords", 3);
		
		objectUnderTest.afterProcess(null, null);
		objectUnderTest.afterProcess(null, null);
		objectUnderTest.afterProcess(null, null);
		// nothing to assert, just look at the console. This is a terrible unit test :(
	}

	private void setValue(ProcessorListener objectUnderTest, String fieldName, Object value) {
		try {
			Field totalRecords = ProcessorListener.class.getDeclaredField(fieldName);
			totalRecords.setAccessible(true);
			totalRecords.set(objectUnderTest, value);
		} catch (Throwable t) {
			String template = "Failed to set field %s to '%s'";
			throw new RuntimeException(String.format(template, fieldName, value), t);
		}
	}
}
