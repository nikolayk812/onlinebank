package com.onlinebank.web;

import com.onlinebank.AppConfig;
import com.onlinebank.LocalAppConfig;
import com.onlinebank.RemoteAppConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;

public class WebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { AppConfig.class, LocalAppConfig.class, RemoteAppConfig.class, WebConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    protected Filter[] getServletFilters() {
        //TODO: add CORS? http://websystique.com/springmvc/spring-mvc-4-restful-web-services-crud-example-resttemplate/
        return null;
    }

}