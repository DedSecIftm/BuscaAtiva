package io.github.dedseciftm.buscaativa.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dedseciftm.buscaativa.dto.AlunoResponse;
import io.github.dedseciftm.buscaativa.exception.AlunoNaoEncontradoException;
import io.github.dedseciftm.buscaativa.model.Aluno;
import io.github.dedseciftm.buscaativa.repository.AlunoRepository;
import io.github.dedseciftm.buscaativa.repository.ResponsavelRepository;
import io.github.dedseciftm.buscaativa.repository.ResponsavelRepository.ContagemPorAluno;
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
		boolean todos = idTurma == null || idTurma.isBlank();
		List<Aluno> alunos = todos ? repository.findAllByOrderByNomeAsc()
				: repository.findAllByTurmaIdTurmaOrderByNomeAsc(idTurma);
		List<ContagemPorAluno> contagens = todos ? responsaveis.contarPorAluno()
				: responsaveis.contarPorAlunoDaTurma(idTurma);

		Map<String, Long> totais = new HashMap<>();
		contagens.forEach(c -> totais.put(c.getIdDoAluno(), c.getTotal()));
		return alunos.stream().map(a -> AlunoResponse.de(a, totais.getOrDefault(a.getIdDoAluno(), 0L))).toList();
	}

	public AlunoResponse buscar(String id) {
		var aluno = repository.findById(id).orElseThrow(() -> new AlunoNaoEncontradoException(id));
		return AlunoResponse.de(aluno, responsaveis.countByAlunoIdDoAluno(id));
	}

}
