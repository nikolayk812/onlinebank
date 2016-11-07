package com.onlinebank;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

import static com.onlinebank.util.Constants.HEROKU;

@Configuration
@Profile(HEROKU)
@PropertySource("classpath:db/heroku/heroku.properties")
public class RemoteAppConfig extends AppConfig {

    @Bean
    @Override
    public DataSource dataSource() {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        String username = System.getenv("JDBC_DATABASE_USERNAME");
        String password = System.getenv("JDBC_DATABASE_PASSWORD");

        return createHikariDataSource(dbUrl, username, password);
    }

}
