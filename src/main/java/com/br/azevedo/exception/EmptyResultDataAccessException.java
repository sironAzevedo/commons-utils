package com.br.azevedo.exception;

import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;

public class EmptyResultDataAccessException extends RuntimeException {

    public EmptyResultDataAccessException(String message, Throwable cause) {
        super(i18n(message), cause);
    }

    public EmptyResultDataAccessException(String message) {
        super(i18n(message));
    }

    public EmptyResultDataAccessException(String messageKey, Object... values) {
        super(i18n(messageKey, values));
    }

    public EmptyResultDataAccessException(Throwable cause, String messageKey, Object... values) {
        super(i18n(messageKey, values), cause);
    }
}
