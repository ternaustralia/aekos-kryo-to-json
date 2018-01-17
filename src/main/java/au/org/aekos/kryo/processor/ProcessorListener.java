package au.org.aekos.kryo.processor;

import java.util.Date;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import au.org.aekos.core.values.Entity;
import au.org.aekos.kryo.DataSourceWrapper;
import au.org.aekos.kryo.JsonWrapper;

@Component
public class ProcessorListener implements ItemProcessListener<Entity, JsonWrapper> {

	private static final Logger logger = LoggerFactory.getLogger(ProcessorListener.class);
    private int processedCounter = 0;
    private boolean isTotalCounted = false;
    private int totalRecords = 0;
    private boolean isStartRecorded = false;
    private long startMs;
    
    @Value("${kryo-crawler.processed-log-threshold:10000}")
    private int processedLogThreshold;
    
    @Autowired
    @Qualifier("aekosDataSource")
    private DataSourceWrapper aekosDataSource;

	@Override
	public void beforeProcess(Entity item) {
		if (isStartRecorded) {
			return;
		}
		isStartRecorded = true;
		startMs = new Date().getTime();
	}

	@Override
	public void afterProcess(Entity item, JsonWrapper result) {
		processedCounter++;
		if (processedCounter % processedLogThreshold == 0) {
			initTotal();
			double donePercent = 100.0 * processedCounter / totalRecords;
			long elapsedMs = new Date().getTime() - startMs;
			String duration = DurationFormatUtils.formatDurationHMS(elapsedMs);
			double msPerThousandRecords = elapsedMs / (processedCounter / 1000.0);
			long remainingMs = calcRemainingMs(processedCounter, totalRecords, elapsedMs);
			String remaining = DurationFormatUtils.formatDurationHMS(remainingMs);
			String template = "Processed %d/%d (%3.1f%%), elapsed %s, remaining %s, avg %1.0fms/1000 records";
			logger.info(String.format(template, processedCounter, totalRecords, donePercent, duration, remaining, msPerThousandRecords));
		}
	}

	static long calcRemainingMs(int processed, int total, long elapsedMs) {
		int remainingRecords = total - processed;
		long msPerRecordProcessed = elapsedMs / processed;
		return remainingRecords * msPerRecordProcessed;
	}

	private void initTotal() {
		if (isTotalCounted) {
			return;
		}
		isTotalCounted = true;
		totalRecords = new JdbcTemplate(aekosDataSource.getDs()).queryForObject("SELECT count(*) FROM __entity", Integer.class);
	}

	@Override
	public void onProcessError(Entity item, Exception e) { }

	public int getProcessedCounter() {
		return processedCounter;
	}
}
