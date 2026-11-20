package io.github.dedseciftm.buscaativa.service;

import java.util.List;

import io.github.dedseciftm.buscaativa.dto.AlunoResponse;
import io.github.dedseciftm.buscaativa.exception.AlunoNaoEncontradoException;
import io.github.dedseciftm.buscaativa.model.Aluno;
import io.github.dedseciftm.buscaativa.repository.AlunoRepository;
import io.github.dedseciftm.buscaativa.repository.ResponsavelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta de alunos. Eles só entram no sistema pela importação, por isso não há cadastro manual. */
@Service
@Transactional(readOnly = true)
public class AlunoService {

	private final AlunoRepository repository;
	private final ResponsavelRepository responsaveis;

	public AlunoService(AlunoRepository repository, ResponsavelRepository responsaveis) {
		this.repository = repository;
		this.responsaveis = responsaveis;
	}

	/** Lista todos os alunos ou, se {@code idTurma} for informado, só os da turma. */
	public List<AlunoResponse> listar(String idTurma) {
		List<Aluno> alunos = (idTurma == null || idTurma.isBlank()) ? repository.findAllByOrderByNomeAsc()
				: repository.findAllByTurmaIdTurmaOrderByNomeAsc(idTurma);
		return alunos.stream().map(this::resposta).toList();
	}

	public AlunoResponse buscar(String id) {
		return resposta(repository.findById(id).orElseThrow(() -> new AlunoNaoEncontradoException(id)));
	}

	private AlunoResponse resposta(Aluno aluno) {
		return AlunoResponse.de(aluno, responsaveis.countByAlunoIdDoAluno(aluno.getIdDoAluno()));
	}

}
