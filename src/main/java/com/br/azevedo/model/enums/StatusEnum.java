package com.br.azevedo.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum StatusEnum {
	TODOS(null),
	ATIVO("Ativo"),
	INATIVO("Inativo");

	private final String descricao;

	public static StatusEnum from(@NonNull String descricao) {
		return Arrays.stream(StatusEnum.values())
				.filter(p -> descricao.equals(p.getDescricao()))
				.findAny()
				.orElseThrow(RuntimeException::new);
	}


}
