package io.github.dedseciftm.buscaativa.dto;

import io.github.dedseciftm.buscaativa.model.NivelEscolar;
import io.github.dedseciftm.buscaativa.model.Periodo;
import io.github.dedseciftm.buscaativa.model.Turma;

public record TurmaResponse(String idTurma, Integer ano, NivelEscolar nivelEscolar, String tipoEnsino,
		Periodo periodo) {

	public static TurmaResponse de(Turma turma) {
		return new TurmaResponse(turma.getIdTurma(), turma.getAno(), turma.getNivelEscolar(), turma.getTipoEnsino(),
				turma.getPeriodo());
	}

}
