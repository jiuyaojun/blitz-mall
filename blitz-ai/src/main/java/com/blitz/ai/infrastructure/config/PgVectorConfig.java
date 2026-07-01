package com.blitz.ai.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@org.springframework.context.annotation.Profile("!minimal")
public class PgVectorConfig {

    @Bean
    @ConfigurationProperties(prefix = "blitz.pg.datasource")
    public DataSource pgDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public JdbcTemplate pgJdbcTemplate(@Qualifier("pgDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    public PgVectorStore pgVectorStore(JdbcTemplate pgJdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(pgJdbcTemplate, embeddingModel).build();
    }
}
