package com.br.azevedo.exception;

import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;

public class EntidadeNaoEncontradaException extends BusinessException {

	private static final long serialVersionUID = 1L;

	private Object objetoRetorno;
	private String classe;
	
	public EntidadeNaoEncontradaException(String message) {
		super(message);
	}
	
	public EntidadeNaoEncontradaException(String message, Object objetoRetorno, String classe) {
		super(i18n(message));
		this.objetoRetorno = objetoRetorno;
		this.classe = classe;
	}
	
	public Object getObjetoRetorno() {
		return objetoRetorno;
	}

	public String getClasse() {
		return classe;
	}
}