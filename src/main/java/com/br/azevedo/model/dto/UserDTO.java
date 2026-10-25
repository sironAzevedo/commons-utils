package com.br.azevedo.model.dto;

import com.br.azevedo.model.enums.PerfilEnum;
import com.br.azevedo.model.enums.UserStatusEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
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

	private String password;

	private String provider;

	private String providerId;

	private UserStatusEnum status;

	private List<PerfilEnum> perfis;

	private String photo;

	private String photoUploaded;

	private Boolean photoUpdate;

	private Boolean termsAgreed;

	public UserDTO(Long id, String name, String email, String password, UserStatusEnum status, List<PerfilEnum> perfis) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.status = status;
		this.perfis = perfis;
	}
}
