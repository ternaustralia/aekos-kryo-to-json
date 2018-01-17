package au.org.aekos.kryo.processor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.Date;

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
		int fiveMinutesAndTwoSecondsAgo = (5 * 60) + 2;
		setValue(objectUnderTest, "isStartRecorded", true);
		setValue(objectUnderTest, "startMs", new Date().getTime() - (fiveMinutesAndTwoSecondsAgo * 1000));
		setValue(objectUnderTest, "processedLogThreshold", 1);
		setValue(objectUnderTest, "isTotalCounted", true);
		setValue(objectUnderTest, "processedCounter", 2230);
		setValue(objectUnderTest, "totalRecords", 2233);
		
		objectUnderTest.afterProcess(null, null);
		objectUnderTest.afterProcess(null, null);
		objectUnderTest.afterProcess(null, null);
		// nothing to assert, just look at the console. This is a terrible unit test :(
	}
	
	/**
	 * Can we calculate how many ms are left?
	 */
	@Test
	public void testCalcRemainingMs01() {
		long result = ProcessorListener.calcRemainingMs(2000, 10000, 10 * 1000);
		assertThat(result, is(40l * 1000));
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
