package au.org.aekos.kryo;

import javax.sql.DataSource;

/**
 * This is a total hack. Spring Batch expects a DataSource to be defined in here
 * but I couldn't get environment injected into it. I think the DataSource needs to exist
 * early in the lifecycle so the JobRepository can store job information in it.
 * 
 * Using this lets us define the HSQL DataSource for the job repo and separately define
 * the DataSource that we need for the actual job.
 */
public class DataSourceWrapper {
	private final DataSource ds;

	public DataSourceWrapper(DataSource ds) {
		this.ds = ds;
	}

	public DataSource getDs() {
		return ds;
	}
}
