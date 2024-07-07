/*
 * Classe responsável por conter as exceções de permissão do projeto Pessoa.
 * 
 * Â© . Copyright. - 2020 Porto Seguro. S.A. - Todos os direitos reservados.
 */
package com.br.azevedo.exception;


import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;

public class PermissaoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PermissaoException(String message, Throwable cause) {
		super(i18n(message), cause);
	}

	public PermissaoException(String message) {
		super(i18n(message));
	}

	public PermissaoException(String messageKey, Object... values) {
		super(i18n(messageKey, values));
	}
}
