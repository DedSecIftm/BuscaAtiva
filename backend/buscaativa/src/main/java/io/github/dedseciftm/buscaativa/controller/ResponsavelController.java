package io.github.dedseciftm.buscaativa.controller;

import java.util.List;
import java.util.UUID;

import io.github.dedseciftm.buscaativa.dto.ResponsavelRequest;
import io.github.dedseciftm.buscaativa.dto.ResponsavelResponse;
import io.github.dedseciftm.buscaativa.service.ResponsavelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Consulta, edição e exclusão de responsáveis; o cadastro é feito em {@code /api/alunos/{id}/responsaveis}. */
@RestController
@RequestMapping("/api/responsaveis")
public class ResponsavelController {

	private final ResponsavelService service;

	public ResponsavelController(ResponsavelService service) {
		this.service = service;
	}

	@GetMapping
	public List<ResponsavelResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{id}")
	public ResponsavelResponse buscar(@PathVariable UUID id) {
		return service.buscar(id);
	}

	@PutMapping("/{id}")
	public ResponsavelResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ResponsavelRequest dados) {
		return service.atualizar(id, dados);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable UUID id) {
		service.excluir(id);
		return ResponseEntity.noContent().build();
	}

}
