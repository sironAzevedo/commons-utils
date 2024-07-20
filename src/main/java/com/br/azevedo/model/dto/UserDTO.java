package com.br.azevedo.model.dto;

import com.br.azevedo.model.enums.PerfilEnum;
import com.br.azevedo.model.enums.UserStatusEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;

	@NotEmpty
	private String name;
	
	@Email
    @NotEmpty
	private String email;

	@NotEmpty
	private String password;

	private UserStatusEnum status;

	private List<PerfilEnum> perfis;

	public UserDTO() {
		super();
	}

}
