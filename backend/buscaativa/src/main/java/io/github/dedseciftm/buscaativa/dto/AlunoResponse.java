package io.github.dedseciftm.buscaativa.dto;

import io.github.dedseciftm.buscaativa.model.Aluno;
import io.github.dedseciftm.buscaativa.model.SituacaoAluno;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record AlunoResponse(
        UUID id,
        String nome,
        String matricula,
        String email,
        String telefone,
        SituacaoAluno situacao,
        String turma,
        Integer faltas,
        Integer periodoEmDias,
        List<ResponsavelResponse> responsaveis,
        Instant criadoEm) {

    public static AlunoResponse de(Aluno aluno) {
        var responsaveis = aluno.getResponsaveis().stream().map(ResponsavelResponse::de)
                .sorted(Comparator.comparing(ResponsavelResponse::nome)).toList();
        return new AlunoResponse(aluno.getId(), aluno.getNome(), aluno.getMatricula(), aluno.getEmail(),
                aluno.getTelefone(), aluno.getSituacao(), aluno.getTurma().getNome(), aluno.getFaltas(),
                aluno.getPeriodoEmDias(), responsaveis, aluno.getCriadoEm());
    }

}
