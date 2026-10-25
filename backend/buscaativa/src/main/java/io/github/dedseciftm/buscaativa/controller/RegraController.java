package io.github.dedseciftm.buscaativa.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import io.github.dedseciftm.buscaativa.dto.RegraRequest;
import io.github.dedseciftm.buscaativa.dto.RegraResponse;
import io.github.dedseciftm.buscaativa.service.RegraService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/regras")
public class RegraController {

	private final RegraService service;

	public RegraController(RegraService service) {
		this.service = service;
	}

	@GetMapping
	public List<RegraResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{id}")
	public RegraResponse buscar(@PathVariable UUID id) {
		return service.buscar(id);
	}

	@PostMapping
	public ResponseEntity<RegraResponse> criar(@Valid @RequestBody RegraRequest dados) {
		var criada = service.criar(dados);
		return ResponseEntity.created(URI.create("/api/regras/" + criada.id())).body(criada);
	}

	@PutMapping("/{id}")
	public RegraResponse atualizar(@PathVariable UUID id, @Valid @RequestBody RegraRequest dados) {
		return service.atualizar(id, dados);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable UUID id) {
		service.excluir(id);
		return ResponseEntity.noContent().build();
	}

}
