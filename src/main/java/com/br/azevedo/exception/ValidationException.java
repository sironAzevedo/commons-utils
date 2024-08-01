package com.br.azevedo.exception;

import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;

public class ValidationException extends RuntimeException {

    public ValidationException(String message, Throwable cause) {
        super(i18n(message), cause);
    }

    public ValidationException(String message) {
        super(i18n(message));
    }

    public ValidationException(String messageKey, Object... values) {
        super(i18n(messageKey, values));
    }

    public ValidationException(Throwable cause, String messageKey, Object... values) {
        super(i18n(messageKey, values), cause);
    }
}
