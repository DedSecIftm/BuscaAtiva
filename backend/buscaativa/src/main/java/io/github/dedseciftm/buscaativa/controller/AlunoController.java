package io.github.dedseciftm.buscaativa.controller;

import java.net.URI;
import java.util.List;

import io.github.dedseciftm.buscaativa.dto.AlunoResponse;
import io.github.dedseciftm.buscaativa.dto.ResponsavelRequest;
import io.github.dedseciftm.buscaativa.dto.ResponsavelResponse;
import io.github.dedseciftm.buscaativa.service.AlunoService;
import io.github.dedseciftm.buscaativa.service.ResponsavelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Alunos são somente leitura (vêm da importação); os responsáveis de cada aluno são cadastrados aqui. */
@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

	private final AlunoService alunos;
	private final ResponsavelService responsaveis;

	public AlunoController(AlunoService alunos, ResponsavelService responsaveis) {
		this.alunos = alunos;
		this.responsaveis = responsaveis;
	}

	@GetMapping
	public List<AlunoResponse> listar(@RequestParam(required = false) String idTurma) {
		return alunos.listar(idTurma);
	}

	@GetMapping("/{id}")
	public AlunoResponse buscar(@PathVariable String id) {
		return alunos.buscar(id);
	}

	@GetMapping("/{id}/responsaveis")
	public List<ResponsavelResponse> listarResponsaveis(@PathVariable String id) {
		return responsaveis.listarDoAluno(id);
	}

	@PostMapping("/{id}/responsaveis")
	public ResponseEntity<ResponsavelResponse> cadastrarResponsavel(@PathVariable String id,
			@Valid @RequestBody ResponsavelRequest dados) {
		var criado = responsaveis.criar(id, dados);
		return ResponseEntity.created(URI.create("/api/responsaveis/" + criado.id())).body(criado);
	}

}
