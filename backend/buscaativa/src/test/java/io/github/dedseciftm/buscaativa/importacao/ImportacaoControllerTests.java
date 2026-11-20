package io.github.dedseciftm.buscaativa.importacao;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.github.dedseciftm.buscaativa.model.NivelEscolar;
import io.github.dedseciftm.buscaativa.repository.AlunoRepository;
import io.github.dedseciftm.buscaativa.repository.TurmaRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Usa o banco de testes; cada teste é desfeito ao final. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "USER")
@Transactional
class ImportacaoControllerTests {

	private static final String[] CABECALHO = { "Turma", "ID SIMADE", "Estudante", "% de presença", "Faltas" };

	@Autowired
	private MockMvc mvc;

	@Autowired
	private TurmaRepository turmas;

	@Autowired
	private AlunoRepository alunos;

	@Test
	void importaTurmaEAlunoDoExemploDaPlanilha() throws Exception {
		var arquivo = planilha(CABECALHO,
				new Object[] { "1º EM REG 1 - 2079040", 6667146d, "Kawan Eustáquio Silva", "20,93", 68d });

		mvc.perform(multipart("/api/importacoes").file(xlsx(arquivo)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.turmasCriadas").value(1))
			.andExpect(jsonPath("$.alunosCriados").value(1))
			.andExpect(jsonPath("$.alunosAtualizados").value(0))
			.andExpect(jsonPath("$.linhasComErro").value(0));

		var turma = turmas.findById("2079040").orElseThrow();
		assertEquals(1, turma.getAno());
		assertEquals(NivelEscolar.MEDIO, turma.getNivelEscolar());
		assertEquals("REG 1", turma.getTipoEnsino());
		var aluno = alunos.findById("6667146").orElseThrow();
		assertEquals("Kawan Eustáquio Silva", aluno.getNome());
		assertEquals("2079040", aluno.getTurma().getIdTurma());
	}

	@Test
	void importarDeNovoAtualizaSemDuplicar() throws Exception {
		var primeira = planilha(CABECALHO, new Object[] { "1º EM REG 1 - 2079040", 1d, "Maria", "90", 2d });
		mvc.perform(multipart("/api/importacoes").file(xlsx(primeira))).andExpect(status().isOk());

		var segunda = planilha(CABECALHO, new Object[] { "1º EM REG 1 - 2079040", 1d, "Maria Souza", "85", 5d });
		mvc.perform(multipart("/api/importacoes").file(xlsx(segunda)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.turmasCriadas").value(0))
			.andExpect(jsonPath("$.alunosCriados").value(0))
			.andExpect(jsonPath("$.alunosAtualizados").value(1));

		assertEquals(1, alunos.count());
		assertEquals("Maria Souza", alunos.findById("1").orElseThrow().getNome());
	}

	@Test
	void alunoRepetidoNoMesmoArquivoVaiComoUmSoEValeAUltimaLinha() throws Exception {
		var arquivo = planilha(CABECALHO, new Object[] { "1º EM REG 1 - 2079040", 7d, "Nome Antigo", "90", 2d },
				new Object[] { "1º EM REG 1 - 2079040", 7d, "Nome Novo", "80", 4d });

		mvc.perform(multipart("/api/importacoes").file(xlsx(arquivo)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.alunosCriados").value(1))
			.andExpect(jsonPath("$.alunosAtualizados").value(0));

		assertEquals("Nome Novo", alunos.findById("7").orElseThrow().getNome());
	}

	@Test
	void linhasInvalidasViramErroEAsDemaisSaoImportadas() throws Exception {
		var arquivo = planilha(CABECALHO, new Object[] { "texto solto", 1d, "Fulano", "90", 2d },
				new Object[] { "1º EM REG 1 - 2079040", 12345678901234567d, "Id Gigante", "90", 2d },
				new Object[] { "1º EM REG 1 - 2079040", 2d, "Beltrana", "75,5", 9d });

		mvc.perform(multipart("/api/importacoes").file(xlsx(arquivo)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.alunosCriados").value(1))
			.andExpect(jsonPath("$.linhasComErro").value(2))
			.andExpect(jsonPath("$.erros", hasSize(2)))
			.andExpect(jsonPath("$.erros[0]").value(startsWith("Linha 2")))
			.andExpect(jsonPath("$.erros[1]").value(startsWith("Linha 3")));

		assertEquals(1, alunos.count());
	}

	@Test
	void importa5000AlunosEmTempoRazoavelEReimportaSemDuplicar() throws Exception {
		var linhas = new Object[5001][];
		linhas[0] = CABECALHO;
		for (int i = 1; i <= 5000; i++) {
			linhas[i] = new Object[] { "1º EM REG 1 - " + (3000 + i % 100), 9000000d + i, "Aluno Numero " + i,
					"80,5", (double) (i % 40) };
		}
		var arquivo = planilha(linhas);

		long inicio = System.nanoTime();
		mvc.perform(multipart("/api/importacoes").file(xlsx(arquivo)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.turmasCriadas").value(100))
			.andExpect(jsonPath("$.alunosCriados").value(5000))
			.andExpect(jsonPath("$.linhasComErro").value(0));
		long segundos = (System.nanoTime() - inicio) / 1_000_000_000;
		System.out.println("[desempenho] importar 5000 alunos: " + segundos + " s");
		assertTrue(segundos < 30, "Importar 5000 alunos levou " + segundos + " s");

		mvc.perform(multipart("/api/importacoes").file(xlsx(arquivo)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.alunosCriados").value(0))
			.andExpect(jsonPath("$.alunosAtualizados").value(5000));
		assertEquals(5000, alunos.count());
		assertEquals(100, turmas.count());
	}

	@Test
	void rejeitaArquivoQueNaoEXlsx() throws Exception {
		mvc.perform(multipart("/api/importacoes")
				.file(new MockMultipartFile("arquivo", "alunos.json", "application/json", "{}".getBytes())))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("Envie um arquivo .xlsx."));
	}

	@Test
	void rejeitaArquivoXlsxCorrompido() throws Exception {
		mvc.perform(multipart("/api/importacoes")
				.file(new MockMultipartFile("arquivo", "alunos.xlsx", "application/octet-stream", "lixo".getBytes())))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rejeitaPlanilhaSemCabecalho() throws Exception {
		var arquivo = planilha(new String[] { "a", "b" });

		mvc.perform(multipart("/api/importacoes").file(xlsx(arquivo)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value(startsWith("Não encontrei o cabeçalho")));
	}

	@Test
	@WithMockUser(roles = "USER")
	void turmasEAlunosImportadosAparecemNasConsultas() throws Exception {
		var arquivo = planilha(CABECALHO, new Object[] { "9º EF REG 2 - 555", 10d, "Ana", "99", 0d });
		mvc.perform(multipart("/api/importacoes").file(xlsx(arquivo))).andExpect(status().isOk());

		mvc.perform(get("/api/turmas"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.idTurma == '555')].nivelEscolar").value("FUNDAMENTAL"));
		mvc.perform(get("/api/alunos").param("idTurma", "555"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].quantidadeResponsaveis").value(0));
	}

	private static MockMultipartFile xlsx(byte[] conteudo) {
		return new MockMultipartFile("arquivo", "alunos.xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", conteudo);
	}

	private static byte[] planilha(Object[]... linhas) throws IOException {
		try (var wb = new XSSFWorkbook(); var saida = new ByteArrayOutputStream()) {
			var aba = wb.createSheet();
			for (int i = 0; i < linhas.length; i++) {
				var row = aba.createRow(i);
				for (int j = 0; j < linhas[i].length; j++) {
					if (linhas[i][j] instanceof Double d) {
						row.createCell(j).setCellValue(d);
					}
					else {
						row.createCell(j).setCellValue(String.valueOf(linhas[i][j]));
					}
				}
			}
			wb.write(saida);
			return saida.toByteArray();
		}
	}

}
