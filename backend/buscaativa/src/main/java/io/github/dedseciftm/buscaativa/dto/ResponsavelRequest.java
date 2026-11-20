package io.github.dedseciftm.buscaativa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados enviados para cadastrar ou editar um responsável. O e-mail é opcional. */
public record ResponsavelRequest(
		@NotBlank(message = "Informe o nome do responsável.")
		@Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
		String nome,

		@NotBlank(message = "Informe o grau de parentesco.")
		@Size(max = 20, message = "O grau de parentesco pode ter no máximo 20 caracteres.")
		String grauParentesco,

		@NotBlank(message = "Informe o telefone.")
		// Só dígitos e símbolos de formatação, com 10 a 13 dígitos no total (DDD + número, com DDI opcional).
		@Pattern(regexp = "^\\+?(?=(?:[^0-9]*[0-9]){10,13}[^0-9]*$)[0-9()\\s.-]+$",
				message = "Informe um telefone válido, com DDD.")
		String telefone,

		@Email(message = "Informe um e-mail válido.")
		@Size(max = 80, message = "O e-mail pode ter no máximo 80 caracteres.")
		String email) {
}
