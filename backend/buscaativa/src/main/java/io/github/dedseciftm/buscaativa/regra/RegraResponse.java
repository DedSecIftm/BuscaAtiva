package io.github.dedseciftm.buscaativa.regra;

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

	static RegraResponse de(RegraIrregularidade regra) {
		return new RegraResponse(regra.getId(), regra.getNome(), regra.getTipo(), regra.getQuantidadeFaltas(),
				regra.getPeriodoEmDias(), regra.isAtiva(), regra.getCriadoEm());
	}

}
