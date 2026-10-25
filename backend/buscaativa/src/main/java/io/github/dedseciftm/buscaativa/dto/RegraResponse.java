package io.github.dedseciftm.buscaativa.dto;

import io.github.dedseciftm.buscaativa.model.RegraIrregularidade;
import io.github.dedseciftm.buscaativa.model.TipoRegra;

import java.time.Instant;
import java.util.UUID;

public record RegraResponse(
		UUID id,
		String nome,
		TipoRegra tipo,
		Integer quantidadeFaltas,
		Integer periodoEmDias,
		boolean ativa,
		Instant criadoEm) {

	public static RegraResponse de(RegraIrregularidade regra) {
		return new RegraResponse(regra.getId(), regra.getNome(), regra.getTipo(), regra.getQuantidadeFaltas(),
				regra.getPeriodoEmDias(), regra.isAtiva(), regra.getCriadoEm());
	}

}
