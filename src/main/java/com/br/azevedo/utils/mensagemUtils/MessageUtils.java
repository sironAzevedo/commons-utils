package com.br.azevedo.utils.mensagemUtils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
public class MessageUtils {
    private static MessageSource messageSource;

    public MessageUtils(@Qualifier("resourceBundleMessageSource") MessageSource messageSource) {
        MessageUtils.messageSource = messageSource;
    }

    public static String i18n(String key, Object... values) {
        try {
            String message = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
            if(!message.equalsIgnoreCase(key)) {
                Object[] objects = Arrays.stream(values).filter(Objects::nonNull).map(Object::toString).toArray();
                return MessageFormat.format(message, objects);
            }
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Erro no messageSource: ", e);
        }
    }

    public static String i18nFile(String key, String fileName, Object... values) {
        ResourceBundle BUNDLE = ResourceBundle.getBundle(fileName);
        String message = BUNDLE.getString(key);
        Object[] objects = Arrays.stream(values).filter(Objects::nonNull).map(Object::toString).toArray();
        return MessageFormat.format(message, objects);
    }
}
