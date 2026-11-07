package io.github.dedseciftm.buscaativa.dto;

import io.github.dedseciftm.buscaativa.model.AlunoResponsavel;
import io.github.dedseciftm.buscaativa.model.Parentesco;

import java.util.UUID;

/** Responsável visto a partir de um aluno: {@code parentesco} é a relação com esse aluno. */
public record ResponsavelResponse(UUID id, String nome, String telefone, String email, Parentesco parentesco) {

    public static ResponsavelResponse de(AlunoResponsavel vinculo) {
        var responsavel = vinculo.getResponsavel();
        return new ResponsavelResponse(responsavel.getId(), responsavel.getNome(), responsavel.getTelefone(),
                responsavel.getEmail(), vinculo.getParentesco());
    }

}
