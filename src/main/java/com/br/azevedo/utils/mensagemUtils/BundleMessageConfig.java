package com.br.azevedo.utils.mensagemUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration
public class BundleMessageConfig {

    @Value("${message.baseName:#{null}}")
    private String baseName;

    @Value("${message.defaultLanguage:pt_BR}")
    private String defaultLanguage;

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();
        acceptHeaderLocaleResolver.setDefaultLocale(new Locale(defaultLanguage));
        return acceptHeaderLocaleResolver;
    }

    @Bean(name = "resourceBundleMessageSource")
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource rs = new ResourceBundleMessageSource();

        String basesName = "exceptionMessages";
        if(StringUtils.isNotEmpty(baseName)) {
            basesName += "," + baseName;
        }
        rs.setBasenames(basesName.split(","));
        rs.setDefaultEncoding("UTF-8");
        rs.setUseCodeAsDefaultMessage(true);
        return rs;
    }
}
