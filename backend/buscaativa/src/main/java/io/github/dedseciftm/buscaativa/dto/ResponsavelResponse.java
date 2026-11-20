package io.github.dedseciftm.buscaativa.dto;

import java.util.UUID;

import io.github.dedseciftm.buscaativa.model.Responsavel;

public record ResponsavelResponse(UUID id, String idDoAluno, String nome, String grauParentesco, String telefone,
		String email) {

	public static ResponsavelResponse de(Responsavel r) {
		return new ResponsavelResponse(r.getId(), r.getAluno().getIdDoAluno(), r.getNome(), r.getGrauParentesco(),
				r.getTelefone(), r.getEmail());
	}

}
