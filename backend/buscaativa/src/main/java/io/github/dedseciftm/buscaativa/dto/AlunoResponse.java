package io.github.dedseciftm.buscaativa.dto;

import io.github.dedseciftm.buscaativa.model.Aluno;
import io.github.dedseciftm.buscaativa.model.SituacaoAluno;

import java.time.Instant;
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
        Instant criadoEm) {

    public static AlunoResponse de(Aluno aluno) {
        return new AlunoResponse(aluno.getId(), aluno.getNome(), aluno.getMatricula(), aluno.getEmail(),
                aluno.getTelefone(), aluno.getSituacao(), aluno.getTurma().getNome(), aluno.getFaltas(),
                aluno.getPeriodoEmDias(), aluno.getCriadoEm());
    }

}
