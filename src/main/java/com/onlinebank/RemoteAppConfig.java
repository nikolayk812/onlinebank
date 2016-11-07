package com.onlinebank;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

import static com.onlinebank.util.Constants.HEROKU;

@Configuration
@Profile(HEROKU)
@PropertySource("classpath:db/heroku/heroku.properties")
public class RemoteAppConfig extends AppConfig {

    @Bean
    @Override
    public DataSource dataSource() throws URISyntaxException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

        return createHikariDataSource(dbUrl, username, password);
    }

}
