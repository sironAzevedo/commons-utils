package com.br.azevedo.utils.mensagemUtils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

@Component
public class MessageUtils {
    private static MessageSource messageSource;

    public MessageUtils(@Qualifier("resourceBundleMessageSource") MessageSource messageSource) {
        MessageUtils.messageSource = messageSource;
    }

    public static void setMessageSource(MessageSource ms) {
        MessageUtils.messageSource = ms;
    }

    public static String i18n(String key, Object... values) {
        if (key == null) {
            return null;
        }
        if (messageSource == null) {
            if (values != null && values.length > 0) {
                Object[] objects = Arrays.stream(values).filter(Objects::nonNull).map(Object::toString).toArray();
                return MessageFormat.format(key, objects);
            }
            return key;
        }
        try {
            String message = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
            if (message != null && !message.equalsIgnoreCase(key)) {
                Object[] objects = Arrays.stream(values).filter(Objects::nonNull).map(Object::toString).toArray();
                return MessageFormat.format(message, objects);
            }
            return key;
        } catch (Exception e) {
            if (values != null && values.length > 0) {
                Object[] objects = Arrays.stream(values).filter(Objects::nonNull).map(Object::toString).toArray();
                return MessageFormat.format(key, objects);
            }
            return key;
        }
    }

    public static String i18nFile(String key, String fileName, Object... values) {
        ResourceBundle BUNDLE = ResourceBundle.getBundle(fileName);
        String message = BUNDLE.getString(key);
        Object[] objects = Arrays.stream(values).filter(Objects::nonNull).map(Object::toString).toArray();
        return MessageFormat.format(message, objects);
    }
}
