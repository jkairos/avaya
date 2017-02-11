package com.avaya.queue.db;

import java.io.File;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.avaya.queue.util.Constants;

public class QueueDataSource {

	// ...

	@Bean
	public DataSource dataSource() {

		// no need shutdown, EmbeddedDatabaseFactoryBean will take care of this
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase db = builder.setType(EmbeddedDatabaseType.HSQL)
				.addScript("file:///"+Constants.APP_PATH+File.separator+"db/create-db.sql")
				.addScript("file:///"+Constants.APP_PATH+File.separator+"db/insert-data.sql").build();
		return db;
	}
}
