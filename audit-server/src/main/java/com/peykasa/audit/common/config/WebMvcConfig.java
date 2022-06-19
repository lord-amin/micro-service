package com.peykasa.audit.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*")
                        .allowedMethods("*").allowCredentials(false);
            }
        };
    }


    @Override
    public void configureMessageConverters(
            List<HttpMessageConverter<?>> converters) {
        // put the jackson converter to the front of the list so that application/json content-type strings will be treated as JSON
        converters.add(new MappingJackson2HttpMessageConverter());
        // and probably needs a string converter too for text/plain content-type strings to be properly handled
        converters.add(new StringHttpMessageConverter());
    }
}

