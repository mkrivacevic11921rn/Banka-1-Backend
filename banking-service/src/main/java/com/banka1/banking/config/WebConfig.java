package com.banka1.banking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final InterbankInterceptor interbankInterceptor;

    public WebConfig(InterbankInterceptor interbankInterceptor) {
        this.interbankInterceptor = interbankInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interbankInterceptor);
    }
}
