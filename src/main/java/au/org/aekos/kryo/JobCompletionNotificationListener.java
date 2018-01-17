package au.org.aekos.kryo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.org.aekos.kryo.processor.ProcessorListener;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	private static final Logger logger = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
	
	@Autowired private ProcessorListener processorListener;

	@Override
	public void afterJob(JobExecution jobExecution) {
		logger.info("Job finished.");
		logger.info("Processed records count: " + processorListener.getProcessedCounter());
	}
}
