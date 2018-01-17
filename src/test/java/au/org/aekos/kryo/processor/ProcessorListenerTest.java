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
		objectUnderTest.afterProcess(null, null);
		objectUnderTest.afterProcess(null, null);
		int result = objectUnderTest.getProcessedCounter();
		assertThat(result, is(2));
	}
	
	/**
	 * Test the progress message.
	 */
	@Test
	public void testGetProcesseunter01() throws NoSuchFieldException, Throwable {
		ProcessorListener objectUnderTest = new ProcessorListener();
		
		Field isStartRecorded = ProcessorListener.class.getDeclaredField("isStartRecorded");
		isStartRecorded.setAccessible(true);
		isStartRecorded.set(objectUnderTest, true);
		Field startMs = ProcessorListener.class.getDeclaredField("startMs");
		startMs.setAccessible(true);
		startMs.set(objectUnderTest, 1516163067016l);
		Field processedLogThreshold = ProcessorListener.class.getDeclaredField("processedLogThreshold");
		processedLogThreshold.setAccessible(true);
		processedLogThreshold.set(objectUnderTest, 1);
		Field isTotalCounted = ProcessorListener.class.getDeclaredField("isTotalCounted");
		isTotalCounted.setAccessible(true);
		isTotalCounted.set(objectUnderTest, true);
		Field totalRecords = ProcessorListener.class.getDeclaredField("totalRecords");
		totalRecords.setAccessible(true);
		totalRecords.set(objectUnderTest, 3);
		
		objectUnderTest.afterProcess(null, null);
		objectUnderTest.afterProcess(null, null);
		objectUnderTest.afterProcess(null, null);
		// nothing to assert, just look at the console. This is a terrible unit test :(
	}
}
