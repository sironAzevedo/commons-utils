package com.br.azevedo.infra.log.xtrid;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value="xtrid",
        havingValue = "true",
        matchIfMissing = true)
public class XTridBuilderFactory {

    @Bean
    public FilterRegistrationBean<XTridContextFilter> xTridFilter() {
        FilterRegistrationBean<XTridContextFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new XTridContextFilter());
        return registrationBean;
    }
}
