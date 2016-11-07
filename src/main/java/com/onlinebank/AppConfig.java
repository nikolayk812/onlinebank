package com.onlinebank;

import com.onlinebank.model.Account;
import com.onlinebank.service.lock.LockService;
import com.onlinebank.util.Constants;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;


@Configuration
@ComponentScan(basePackages = "com.onlinebank.repo, com.onlinebank.service")
@EnableJpaRepositories("com.onlinebank")
@EnableTransactionManagement
public class AppConfig {

    @Configuration
    @Profile(Constants.HEROKU)
    @PropertySource("classpath:db/heroku/heroku.properties")
    static class Heroku {
    }

    @Configuration
    @Profile(Constants.POSTGRES)
    @PropertySource("classpath:db/postgres/postgres.properties")
    static class Postgres {
    }

    @Configuration
    @Profile(Constants.H2)
    @PropertySource("classpath:db/h2/h2.properties")
    static class H2 {
    }

    @Value("${jdbc.driverClassName}")
    private String driverClassName;
    @Value("${jdbc.url}")
    private String url;
    @Value("${jdbc.username}")
    private String username;
    @Value("${jdbc.password}")
    private String password;

    @Value("${hibernate.dialect}")
    private String hibernateDialect;
    @Value("${hibernate.show_sql}")
    private String hibernateShowSql;
    @Value("${hibernate.format_sql}")
    private String hibernateFormatSql;
    @Value("${hibernate.hbm2ddl.auto}")
    private String hibernateHbm2ddlAuto;

    @Value("${hikari.poolsize}")
    private int poolSize;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.onlinebank.model");
        em.setJpaVendorAdapter(vendorAdapter());
        em.setJpaProperties(additionalProperties());
        return em;
    }

    @Bean
    public JpaVendorAdapter vendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.addDataSourceProperty("user", username);
        dataSource.addDataSourceProperty("password", password);
        dataSource.setMaximumPoolSize(poolSize);
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        transactionManager.setJpaDialect(vendorAdapter().getJpaDialect());
        return transactionManager;
    }

    private Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", hibernateHbm2ddlAuto);
        properties.setProperty("hibernate.dialect", hibernateDialect);
        properties.setProperty("hibernate.show_sql", hibernateShowSql);
        properties.setProperty("hibernate.format_sql", hibernateFormatSql);
        return properties;
    }

    @Bean
    public LockService<Integer, Account> accountLockService() {
        return new LockService<>();
    }
}