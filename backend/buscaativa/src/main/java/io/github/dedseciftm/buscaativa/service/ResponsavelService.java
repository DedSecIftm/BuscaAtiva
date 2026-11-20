package io.github.dedseciftm.buscaativa.service;

import java.util.List;
import java.util.UUID;

import io.github.dedseciftm.buscaativa.dto.ResponsavelRequest;
import io.github.dedseciftm.buscaativa.dto.ResponsavelResponse;
import io.github.dedseciftm.buscaativa.exception.AlunoNaoEncontradoException;
import io.github.dedseciftm.buscaativa.exception.ResponsavelNaoEncontradoException;
import io.github.dedseciftm.buscaativa.model.Responsavel;
import io.github.dedseciftm.buscaativa.repository.AlunoRepository;
import io.github.dedseciftm.buscaativa.repository.ResponsavelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ResponsavelService {

	private final ResponsavelRepository repository;
	private final AlunoRepository alunos;

	public ResponsavelService(ResponsavelRepository repository, AlunoRepository alunos) {
		this.repository = repository;
		this.alunos = alunos;
	}

	public List<ResponsavelResponse> listar() {
		return repository.findAllByOrderByNomeAsc().stream().map(ResponsavelResponse::de).toList();
	}

	public List<ResponsavelResponse> listarDoAluno(String idDoAluno) {
		if (!alunos.existsById(idDoAluno)) {
			throw new AlunoNaoEncontradoException(idDoAluno);
		}
		return repository.findAllByAlunoIdDoAlunoOrderByNomeAsc(idDoAluno).stream().map(ResponsavelResponse::de)
			.toList();
	}

	public ResponsavelResponse buscar(UUID id) {
		return ResponsavelResponse.de(encontrar(id));
	}

	@Transactional
	public ResponsavelResponse criar(String idDoAluno, ResponsavelRequest dados) {
		var aluno = alunos.findById(idDoAluno).orElseThrow(() -> new AlunoNaoEncontradoException(idDoAluno));
		var responsavel = new Responsavel(aluno, dados.nome().trim(), dados.grauParentesco().trim(),
				somenteDigitos(dados.telefone()), emailDe(dados));
		return ResponsavelResponse.de(repository.saveAndFlush(responsavel));
	}

	@Transactional
	public ResponsavelResponse atualizar(UUID id, ResponsavelRequest dados) {
		var responsavel = encontrar(id);
		responsavel.setNome(dados.nome().trim());
		responsavel.setGrauParentesco(dados.grauParentesco().trim());
		responsavel.setTelefone(somenteDigitos(dados.telefone()));
		responsavel.setEmail(emailDe(dados));
		return ResponsavelResponse.de(responsavel);
	}

	@Transactional
	public void excluir(UUID id) {
		repository.delete(encontrar(id));
	}

	private Responsavel encontrar(UUID id) {
		return repository.findById(id).orElseThrow(() -> new ResponsavelNaoEncontradoException(id));
	}

	// Guarda só os dígitos (com DDI, se houver), formato esperado para envio pelo WhatsApp.
	private static String somenteDigitos(String telefone) {
		return telefone.replaceAll("\\D", "");
	}

	// E-mail é opcional: vazio vira nulo.
	private static String emailDe(ResponsavelRequest dados) {
		return dados.email() == null || dados.email().isBlank() ? null : dados.email().trim();
	}

}
