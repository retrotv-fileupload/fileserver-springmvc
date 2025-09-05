package dev.retrotv.fileserver.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.retrotv.fileserver.common.filter.FileDownloadFilter;
import dev.retrotv.fileserver.common.filter.RequestLoggingFilter;
import dev.retrotv.fileserver.common.filter.ResponseLoggingFilter;

@Configuration
public class FilterConfig {
    
    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestLoggingFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<ResponseLoggingFilter> responseLoggingFilter() {
        FilterRegistrationBean<ResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ResponseLoggingFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<FileDownloadFilter> fileDownloadFilter() {
        FilterRegistrationBean<FileDownloadFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new FileDownloadFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(3);
        return registrationBean;
    }
}
