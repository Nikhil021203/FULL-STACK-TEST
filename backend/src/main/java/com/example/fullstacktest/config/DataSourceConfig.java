package com.example.fullstacktest.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Local development uses the normal JDBC settings in application.yml.
 * Render supplies DATABASE_URL as postgresql://user:password@host:port/database,
 * so this configuration converts it to the JDBC URL and credentials Hikari needs.
 */
@Configuration
public class DataSourceConfig {
    @Bean
    DataSource dataSource(Environment environment) {
        String renderDatabaseUrl = environment.getProperty("DATABASE_URL");
        if (renderDatabaseUrl != null && !renderDatabaseUrl.isBlank()) {
            RenderDatabaseConnection connection = parseRenderDatabaseUrl(renderDatabaseUrl);
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(connection.jdbcUrl())
                    .username(connection.username())
                    .password(connection.password())
                    .build();
        }

        return DataSourceBuilder.create()
                .driverClassName(environment.getProperty("spring.datasource.driver-class-name", "org.postgresql.Driver"))
                .url(environment.getRequiredProperty("spring.datasource.url"))
                .username(environment.getRequiredProperty("spring.datasource.username"))
                .password(environment.getRequiredProperty("spring.datasource.password"))
                .build();
    }

    private RenderDatabaseConnection parseRenderDatabaseUrl(String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        if (!"postgres".equals(uri.getScheme()) && !"postgresql".equals(uri.getScheme())) {
            throw new IllegalArgumentException("DATABASE_URL must use the postgres or postgresql scheme.");
        }
        if (uri.getHost() == null || uri.getRawPath() == null || uri.getRawPath().isBlank()) {
            throw new IllegalArgumentException("DATABASE_URL must include a database host and name.");
        }
        String rawUserInfo = uri.getRawUserInfo();
        if (rawUserInfo == null || !rawUserInfo.contains(":")) {
            throw new IllegalArgumentException("DATABASE_URL must include a username and password.");
        }

        String[] credentials = rawUserInfo.split(":", 2);
        String port = uri.getPort() == -1 ? "" : ":" + uri.getPort();
        String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + port + uri.getRawPath() + query;
        return new RenderDatabaseConnection(
                jdbcUrl,
                decode(credentials[0]),
                decode(credentials[1]));
    }

    private String decode(String value) {
        // URLDecoder treats '+' as a form-space. In a database password it is a literal plus.
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    private record RenderDatabaseConnection(String jdbcUrl, String username, String password) {
    }
}
