package com.br.azevedo.exception;

import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message, Throwable cause) {
        super(i18n(message), cause);
    }

    public AuthenticationException(String message) {
        super(i18n(message));
    }

    public AuthenticationException(String messageKey, Object... values) {
        super(i18n(messageKey, values));
    }

    public AuthenticationException(Throwable cause, String messageKey, Object... values) {
        super(i18n(messageKey, values), cause);
    }
}
