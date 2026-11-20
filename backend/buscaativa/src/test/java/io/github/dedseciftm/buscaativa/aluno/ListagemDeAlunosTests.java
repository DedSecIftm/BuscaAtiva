package io.github.dedseciftm.buscaativa.aluno;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import io.github.dedseciftm.buscaativa.model.Aluno;
import io.github.dedseciftm.buscaativa.model.NivelEscolar;
import io.github.dedseciftm.buscaativa.model.Responsavel;
import io.github.dedseciftm.buscaativa.model.Turma;
import io.github.dedseciftm.buscaativa.repository.AlunoRepository;
import io.github.dedseciftm.buscaativa.repository.ResponsavelRepository;
import io.github.dedseciftm.buscaativa.repository.TurmaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Garante que a listagem não faz uma consulta por aluno: com a escola cheia isso viraria milhares de idas ao banco
 * (requisito de bom desempenho com muitos alunos).
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "USER")
@Transactional
class ListagemDeAlunosTests {

	private static final int TURMAS = 5;
	private static final int ALUNOS_POR_TURMA = 40;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private TurmaRepository turmas;

	@Autowired
	private AlunoRepository alunos;

	@Autowired
	private ResponsavelRepository responsaveis;

	@Autowired
	private EntityManagerFactory fabrica;

	@Autowired
	private EntityManager em;

	@Test
	void listarAlunosNaoFazUmaConsultaPorAluno() throws Exception {
		var novosResponsaveis = new ArrayList<Responsavel>();
		for (int t = 0; t < TURMAS; t++) {
			var turma = turmas.save(new Turma("LT" + t, 1, NivelEscolar.MEDIO, "REG 1", null));
			for (int a = 0; a < ALUNOS_POR_TURMA; a++) {
				// Id atribuído à mão: save() devolve a cópia gerenciada, que é a que o responsável precisa referenciar.
				var aluno = alunos.save(new Aluno("LA" + t + "-" + a, turma, "Aluno " + t + " " + a));
				if (a % 2 == 0) {
					novosResponsaveis.add(new Responsavel(aluno, "Resp " + a, "Mãe", "34999990000", null));
				}
			}
		}
		responsaveis.saveAll(novosResponsaveis);
		em.flush();
		em.clear();

		var estatisticas = fabrica.unwrap(SessionFactory.class).getStatistics();
		estatisticas.clear();

		mvc.perform(get("/api/alunos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(TURMAS * ALUNOS_POR_TURMA)))
			.andExpect(jsonPath("$[?(@.idDoAluno == 'LA0-0')].quantidadeResponsaveis").value(1))
			.andExpect(jsonPath("$[?(@.idDoAluno == 'LA0-1')].quantidadeResponsaveis").value(0));

		long consultas = estatisticas.getPrepareStatementCount();
		assertTrue(consultas <= 5, "A listagem de " + (TURMAS * ALUNOS_POR_TURMA) + " alunos fez " + consultas
				+ " consultas ao banco; esperado no máximo 5.");
	}

	@Test
	void listarResponsaveisNaoFazUmaConsultaPorResponsavel() throws Exception {
		var turma = turmas.save(new Turma("LT-R", 1, NivelEscolar.MEDIO, "REG 1", null));
		for (int a = 0; a < ALUNOS_POR_TURMA; a++) {
			var aluno = alunos.save(new Aluno("LR-" + a, turma, "Aluno R " + a));
			responsaveis.save(new Responsavel(aluno, "Resp R " + a, "Pai", "34999990000", null));
		}
		em.flush();
		em.clear();

		var estatisticas = fabrica.unwrap(SessionFactory.class).getStatistics();
		estatisticas.clear();

		mvc.perform(get("/api/responsaveis"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(ALUNOS_POR_TURMA)))
			.andExpect(jsonPath("$[0].idDoAluno").isNotEmpty());

		long consultas = estatisticas.getPrepareStatementCount();
		assertTrue(consultas <= 5, "A listagem de responsáveis fez " + consultas + " consultas; esperado no máximo 5.");
	}

	@Test
	void listarAlunosDeUmaTurmaTambemNaoFazUmaConsultaPorAluno() throws Exception {
		var turma = turmas.save(new Turma("LT-X", 1, NivelEscolar.MEDIO, "REG 1", null));
		for (int a = 0; a < ALUNOS_POR_TURMA; a++) {
			alunos.save(new Aluno("LX-" + a, turma, "Aluno X " + a));
		}
		em.flush();
		em.clear();

		var estatisticas = fabrica.unwrap(SessionFactory.class).getStatistics();
		estatisticas.clear();

		mvc.perform(get("/api/alunos").param("idTurma", "LT-X"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(ALUNOS_POR_TURMA)));

		long consultas = estatisticas.getPrepareStatementCount();
		assertTrue(consultas <= 5, "A listagem da turma fez " + consultas + " consultas; esperado no máximo 5.");
	}

}
