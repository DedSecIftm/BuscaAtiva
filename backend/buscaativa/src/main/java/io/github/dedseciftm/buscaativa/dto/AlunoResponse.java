package io.github.dedseciftm.buscaativa.dto;

import io.github.dedseciftm.buscaativa.model.Aluno;

public record AlunoResponse(String idDoAluno, String nome, TurmaResponse turma, long quantidadeResponsaveis) {

	public static AlunoResponse de(Aluno aluno, long quantidadeResponsaveis) {
		return new AlunoResponse(aluno.getIdDoAluno(), aluno.getNome(), TurmaResponse.de(aluno.getTurma()),
				quantidadeResponsaveis);
	}

}
