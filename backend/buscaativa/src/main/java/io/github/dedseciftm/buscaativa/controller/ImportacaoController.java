package io.github.dedseciftm.buscaativa.controller;

import java.io.IOException;

import io.github.dedseciftm.buscaativa.importacao.ImportacaoService;
import io.github.dedseciftm.buscaativa.importacao.PlanilhaInvalidaException;
import io.github.dedseciftm.buscaativa.importacao.ResultadoDaImportacao;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/importacoes")
public class ImportacaoController {

	private final ImportacaoService service;

	public ImportacaoController(ImportacaoService service) {
		this.service = service;
	}

	/** Recebe o .xlsx no campo {@code arquivo} e cria ou atualiza as turmas e os alunos. */
	@PostMapping(consumes = "multipart/form-data")
	public ResultadoDaImportacao importar(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {

		var nome = arquivo.getOriginalFilename();

		if (arquivo.isEmpty() || nome == null || !nome.toLowerCase().endsWith(".xlsx")) {
			throw new PlanilhaInvalidaException("Envie um arquivo .xlsx.");
		}

		try (var conteudo = arquivo.getInputStream()) {
			return service.importar(conteudo);
		}
		catch (org.apache.poi.EmptyFileException | org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException e) {
			throw new PlanilhaInvalidaException("O arquivo não é uma planilha .xlsx válida.");
		}
	}

}
