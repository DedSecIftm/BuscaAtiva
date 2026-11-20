package io.github.dedseciftm.buscaativa.controller;

import java.util.List;

import io.github.dedseciftm.buscaativa.dto.TurmaResponse;
import io.github.dedseciftm.buscaativa.service.TurmaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

	private final TurmaService service;

	public TurmaController(TurmaService service) {
		this.service = service;
	}

	@GetMapping
	public List<TurmaResponse> listar() {
		return service.listar();
	}

}
