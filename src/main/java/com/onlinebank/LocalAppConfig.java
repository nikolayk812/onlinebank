package com.onlinebank;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

import static com.onlinebank.util.Constants.H2;
import static com.onlinebank.util.Constants.POSTGRES;

@Configuration
@Profile({H2, POSTGRES})
public class LocalAppConfig extends AppConfig {
    @Value("${jdbc.url}")
    private String url;
    @Value("${jdbc.username}")
    private String username;
    @Value("${jdbc.password}")
    private String password;

    @Configuration
    @Profile(POSTGRES)
    @PropertySource("classpath:db/postgres/postgres.properties")
    static class PostgresProperties {
    }

    @Configuration
    @Profile(H2)
    @PropertySource("classpath:db/h2/h2.properties")
    static class H2Properties {
    }

    @Bean
    public DataSource dataSource() {
        return createHikariDataSource(url, username, password);
    }

}
