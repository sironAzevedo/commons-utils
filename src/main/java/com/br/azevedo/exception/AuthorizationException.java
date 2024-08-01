package com.br.azevedo.exception;

import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;

public class AuthorizationException extends RuntimeException {

    public AuthorizationException(String message, Throwable cause) {
        super(i18n(message), cause);
    }

    public AuthorizationException(String message) {
        super(i18n(message));
    }

    public AuthorizationException(String messageKey, Object... values) {
        super(i18n(messageKey, values));
    }

    public AuthorizationException(Throwable cause, String messageKey, Object... values) {
        super(i18n(messageKey, values), cause);
    }
}
