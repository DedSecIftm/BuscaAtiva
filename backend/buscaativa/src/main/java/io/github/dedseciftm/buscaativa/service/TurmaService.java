package io.github.dedseciftm.buscaativa.service;

import java.util.List;

import io.github.dedseciftm.buscaativa.dto.TurmaResponse;
import io.github.dedseciftm.buscaativa.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta de turmas. Elas só entram no sistema pela importação. */
@Service
@Transactional(readOnly = true)
public class TurmaService {

	private final TurmaRepository repository;

	public TurmaService(TurmaRepository repository) {
		this.repository = repository;
	}

	public List<TurmaResponse> listar() {
		return repository.findAllByOrderByIdTurmaAsc().stream().map(TurmaResponse::de).toList();
	}

}
