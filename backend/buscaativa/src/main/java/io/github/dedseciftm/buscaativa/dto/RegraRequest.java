package io.github.dedseciftm.buscaativa.dto;

import io.github.dedseciftm.buscaativa.model.RegraIrregularidade;
import io.github.dedseciftm.buscaativa.model.TipoRegra;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados para criar ou editar uma regra. {@code ativa} é opcional: na criação vale
 * {@code true} e na edição mantém o valor atual.
 */
public record RegraRequest(
		@NotBlank(message = "Dê um nome para a regra.")
		@Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
		String nome,

		@NotNull(message = "Escolha como as faltas devem ser contadas.")
		TipoRegra tipo,

		@NotNull(message = "Informe a quantidade de faltas.")
		@Min(value = 1, message = "Informe um número de faltas maior que zero.")
		Integer quantidadeFaltas,

		@Min(value = 1, message = "Informe um período maior que zero.")
		Integer periodoEmDias,

		Boolean ativa) {

	@AssertTrue(message = "O período precisa ter pelo menos a mesma quantidade de dias que as faltas.")
	public boolean isPeriodoValido() {
		return RegraIrregularidade.periodoValido(tipo, quantidadeFaltas, periodoEmDias);
	}

}
