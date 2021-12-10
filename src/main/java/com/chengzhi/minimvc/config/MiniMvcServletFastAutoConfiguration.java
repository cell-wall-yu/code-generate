package com.chengzhi.minimvc.config;

import com.chengzhi.minimvc.support.MiniMvcServletFast;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({ServletWebServerFactoryAutoConfiguration.class})
public class MiniMvcServletFastAutoConfiguration implements ApplicationContextAware {
    public static final String DEFAULT_MINI_MVC_SERVLET_BEAN_NAME = "miniMvcServletFast";
    public static final String DEFAULT_MINI_MVC_SERVLET_REGISTRATION_BEAN_NAME = "miniMvcServletFastRegistration";
    public ApplicationContext applicationContext;

    public MiniMvcServletFastAutoConfiguration() {
    }

    @Bean(name = {DEFAULT_MINI_MVC_SERVLET_BEAN_NAME})
    public ServletRegistrationBean miniMvcServletFastRegistration() {
        MiniMvcServletFast.setApplicationContext(this.applicationContext);
        ServletRegistrationBean registration = new ServletRegistrationBean(new MiniMvcServletFast(), "/static/", "*.json");
        registration.setName(DEFAULT_MINI_MVC_SERVLET_REGISTRATION_BEAN_NAME);
        return registration;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
