package io.github.dedseciftm.buscaativa.controller;

import io.github.dedseciftm.buscaativa.dto.AlunoRequest;
import io.github.dedseciftm.buscaativa.dto.AlunoResponse;
import io.github.dedseciftm.buscaativa.dto.ImportacaoAlunosRequest;
import io.github.dedseciftm.buscaativa.dto.ImportacaoAlunosResponse;
import io.github.dedseciftm.buscaativa.service.AlunoService;
import io.github.dedseciftm.buscaativa.service.ImportacaoAlunosService;
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

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService service;
    private final ImportacaoAlunosService importacaoService;

    public AlunoController(AlunoService service, ImportacaoAlunosService importacaoService) {
        this.service = service;
        this.importacaoService = importacaoService;
    }

    @GetMapping
    public List<AlunoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public AlunoResponse buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<AlunoResponse> criar(@Valid @RequestBody AlunoRequest dados) {
        var criado = service.criar(dados);
        return ResponseEntity.created(URI.create("/api/alunos/" + criado.id())).body(criado);
    }

    @PostMapping("/importacao")
    public ImportacaoAlunosResponse importar(@Valid @RequestBody ImportacaoAlunosRequest dados) {
        return importacaoService.importar(dados.alunos());
    }

    @PutMapping("/{id}")
    public AlunoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AlunoRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

}
