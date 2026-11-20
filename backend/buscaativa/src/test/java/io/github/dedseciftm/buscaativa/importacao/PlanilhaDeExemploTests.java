package io.github.dedseciftm.buscaativa.importacao;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;

import io.github.dedseciftm.buscaativa.model.NivelEscolar;
import io.github.dedseciftm.buscaativa.repository.AlunoRepository;
import io.github.dedseciftm.buscaativa.repository.TurmaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Usa a planilha de exemplo real ({@code exemploDados.xlsx}): 37 alunos de uma turma, com colunas extras
 * (J e K) que não fazem parte da importação e devem ser ignoradas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "USER")
@Transactional
class PlanilhaDeExemploTests {

	private static final String RECURSO = "importacao/exemploDados.xlsx";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private TurmaRepository turmas;

	@Autowired
	private AlunoRepository alunos;

	@Test
	void leitorLe37AlunosSemErrosEIgnoraColunasExtras() throws IOException {
		try (var arquivo = new ClassPathResource(RECURSO).getInputStream()) {
			var resultado = new LeitorDePlanilha().ler(arquivo);

			assertEquals(37, resultado.linhas().size());
			assertTrue(resultado.erros().isEmpty(), () -> "erros inesperados: " + resultado.erros());

			var primeira = resultado.linhas().get(0);
			assertEquals("1234567", primeira.idDoAluno());
			assertEquals("LUCAS GABRIEL ALMEIDA SILVA", primeira.nome());
			assertEquals(0, new BigDecimal("20.93").compareTo(primeira.percentualPresenca()));
			assertEquals(68, primeira.faltas());
			assertEquals("2020022", primeira.turma().idTurma());
			assertEquals(1, primeira.turma().ano());
			assertEquals(NivelEscolar.MEDIO, primeira.turma().nivelEscolar());
			assertEquals("REG 1", primeira.turma().tipoEnsino());

			// IDs de 8 dígitos (a última linha) também passam, sem notação científica.
			assertEquals("12345682", resultado.linhas().get(36).idDoAluno());
		}
	}

	@Test
	void importaA_PlanilhaDeExemplo_ECadaAlunoFicaNaTurmaCerta() throws Exception {
		mvc.perform(multipart("/api/importacoes").file(arquivo()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.turmasCriadas").value(1))
			.andExpect(jsonPath("$.alunosCriados").value(37))
			.andExpect(jsonPath("$.alunosAtualizados").value(0))
			.andExpect(jsonPath("$.linhasComErro").value(0));

		assertEquals(1, turmas.count());
		assertEquals(37, alunos.count());
		assertEquals("MARIANA COSTA OLIVEIRA", alunos.findById("1234568").orElseThrow().getNome());
		assertEquals("2020022", alunos.findById("12345682").orElseThrow().getTurma().getIdTurma());

		mvc.perform(get("/api/alunos").param("idTurma", "2020022"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(37)));
	}

	@Test
	void importarDeNovoNaoDuplicaNada() throws Exception {
		mvc.perform(multipart("/api/importacoes").file(arquivo())).andExpect(status().isOk());

		mvc.perform(multipart("/api/importacoes").file(arquivo()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.turmasCriadas").value(0))
			.andExpect(jsonPath("$.alunosCriados").value(0))
			.andExpect(jsonPath("$.alunosAtualizados").value(37))
			.andExpect(jsonPath("$.linhasComErro").value(0));

		assertEquals(37, alunos.count());
		assertEquals(1, turmas.count());
	}

	private static MockMultipartFile arquivo() throws IOException {
		try (var conteudo = new ClassPathResource(RECURSO).getInputStream()) {
			return new MockMultipartFile("arquivo", "exemploDados.xlsx",
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", conteudo.readAllBytes());
		}
	}

}
