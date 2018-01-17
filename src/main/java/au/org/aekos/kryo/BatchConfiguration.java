package au.org.aekos.kryo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StreamUtils;

import au.org.aekos.core.values.Entity;
import au.org.aekos.kryo.processor.EntityProcessor;
import au.org.aekos.kryo.processor.ProcessorListener;
import au.org.aekos.kryo.reader.EntityRowMapper;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Value("${kryo-crawler.jdbc.url}") private String jdbcUrl;
    @Value("${kryo-crawler.jdbc.user}") private String jdbcUser;
    @Value("${kryo-crawler.jdbc.password}") private String jdbcPassword;
    @Value("${kryo-crawler.jdbc.fetch-size:0}") private int jdbcFetchSize;
    @Value("${kryo-crawler.chunk-size:1}") private int chunkSize;
    
    @Bean
    public ItemReader<Entity> reader(String allEntitiesQuery, @Qualifier("aekosDataSource") DataSourceWrapper aekosDataSource, RowMapper<Entity> entityRowMapper) {
        JdbcCursorItemReader<Entity> result = new JdbcCursorItemReader<>();
        result.setDataSource(aekosDataSource.getDs());
        result.setSql(allEntitiesQuery);
        result.setRowMapper(entityRowMapper);
        result.setFetchSize(jdbcFetchSize);
		return result;
    }
    
    @Bean
    public ItemProcessor<Entity, JsonWrapper> processor() {
        return new EntityProcessor();
    }

    @Bean
    public ItemWriter<JsonWrapper> writer(@Qualifier("aekosDataSource") DataSourceWrapper aekosDataSource) {
    	JdbcBatchItemWriter<JsonWrapper> result = new JdbcBatchItemWriter<>();
    	result.setDataSource(aekosDataSource.getDs());
    	result.setSql("UPDATE __entity SET entry_json = :json::json WHERE internal_entity_id = :entityId");
    	result.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
    	result.afterPropertiesSet(); // FIXME do we need to call this?
		return result;
    }

    @Bean
    public RowMapper<Entity> entityRowMapper() {
    	return new EntityRowMapper();
    }
    
    @Bean
    public Tasklet prepDb(@Qualifier("aekosDataSource") DataSourceWrapper aekosDataSource) {
    	PrepDb result = new PrepDb();
    	JdbcTemplate jdbcTemplate = new JdbcTemplate(aekosDataSource.getDs());
		result.setJdbcTemplate(jdbcTemplate);
		return result;
    }
    
	@Bean
    public Job apiDataJob(JobCompletionNotificationListener listener, Step prepDbStep, Step transformStep) {
        return jobBuilderFactory.get("KryoToJsonJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(prepDbStep)
                .next(transformStep)
                .build();
    }
    
	@Bean
    public Step prepDbStep(Tasklet prepDb) {
        return stepBuilderFactory.get("stepPrepDb")
        		.tasklet(prepDb)
        		.build();
    }
	
    @Bean
    public Step transformStep(ItemReader<Entity> reader, ItemProcessor<Entity, JsonWrapper> processor,
    		ItemWriter<JsonWrapper> writer, ProcessorListener listener) {
        return stepBuilderFactory.get("stepTransform")
                .<Entity, JsonWrapper> chunk(chunkSize)
                .faultTolerant()
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(listener)
                .build();
    }
    
    @Bean
    public DataSource batchDataSource() {
    	BasicDataSource result = new BasicDataSource();
    	result.setUrl("jdbc:hsqldb:file:jobrepo/jobrepo.db;shutdown=true");
    	result.setUsername("sa");
    	result.setPassword("");
    	result.setDriverClassName("org.hsqldb.jdbcDriver");
		return result;
    }
    
    @Bean
    public DataSourceWrapper aekosDataSource() {
    	org.apache.tomcat.jdbc.pool.DataSource result = new org.apache.tomcat.jdbc.pool.DataSource();
    	result.setUrl(jdbcUrl);
    	result.setUsername(jdbcUser);
    	result.setPassword(jdbcPassword);
    	result.setDriverClassName("org.postgresql.Driver");
		return new DataSourceWrapper(result);
    }
    
    @Bean
    public String allEntitiesQuery() throws IOException {
		return getQuery("all-entities.sql");
    }

	private String getQuery(String fileName) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/kryo/sql/" + fileName);
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(is, out);
		return out.toString();
	}
}
