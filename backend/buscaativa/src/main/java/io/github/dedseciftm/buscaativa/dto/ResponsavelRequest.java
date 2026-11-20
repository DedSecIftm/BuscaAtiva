package io.github.dedseciftm.buscaativa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados enviados para cadastrar ou editar um responsável. O e-mail é opcional. */
public record ResponsavelRequest(
		@NotBlank(message = "Informe o nome do responsável.")
		@Size(max = 100, message = "O nome pode ter no máximo 150 caracteres.")
		String nome,

		@NotBlank(message = "Informe o grau de parentesco.")
		@Size(max = 20, message = "O grau de parentesco pode ter no máximo 50 caracteres.")
		String grauParentesco,

		@NotBlank(message = "Informe o telefone.")
		@Pattern(regexp = "^\\+?[0-9()\\s.-]{10,20}$", message = "Informe um telefone válido, com DDD.")
		String telefone,

		@Email(message = "Informe um e-mail válido.")
		@Size(max = 80, message = "O e-mail pode ter no máximo 200 caracteres.")
		String email) {
}
