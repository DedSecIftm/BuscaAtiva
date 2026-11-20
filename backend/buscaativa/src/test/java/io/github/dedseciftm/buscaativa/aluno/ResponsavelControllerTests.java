package io.github.dedseciftm.buscaativa.aluno;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import io.github.dedseciftm.buscaativa.model.Aluno;
import io.github.dedseciftm.buscaativa.model.NivelEscolar;
import io.github.dedseciftm.buscaativa.model.Periodo;
import io.github.dedseciftm.buscaativa.model.Responsavel;
import io.github.dedseciftm.buscaativa.model.Turma;
import io.github.dedseciftm.buscaativa.repository.AlunoRepository;
import io.github.dedseciftm.buscaativa.repository.ResponsavelRepository;
import io.github.dedseciftm.buscaativa.repository.TurmaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Usa o banco do docker-compose; cada teste é desfeito ao final. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "USER")
@Transactional
class ResponsavelControllerTests {

	private static final String ALUNO = "T-ALUNO-1";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private TurmaRepository turmas;

	@Autowired
	private AlunoRepository alunos;

	@Autowired
	private ResponsavelRepository responsaveis;

	@BeforeEach
	void criaAluno() {
		var turma = turmas.save(new Turma("T-TURMA-1", 2026, NivelEscolar.MEDIO, "REG 1", Periodo.MANHA));
		alunos.save(new Aluno(ALUNO, turma, "Maria da Silva"));
	}

	@Test
	void cadastraResponsavelNormalizandoTelefone() throws Exception {
		mvc.perform(post("/api/alunos/" + ALUNO + "/responsaveis").contentType(MediaType.APPLICATION_JSON).content("""
				{ "nome": "  Ana Silva ", "grauParentesco": "Mãe", "telefone": "(34) 99999-1234" }
				"""))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.idDoAluno").value(ALUNO))
			.andExpect(jsonPath("$.nome").value("Ana Silva"))
			.andExpect(jsonPath("$.telefone").value("34999991234"))
			.andExpect(jsonPath("$.email").value(nullValue()));
	}

	@Test
	void rejeitaCadastroSemCamposObrigatoriosOuComEmailInvalido() throws Exception {
		mvc.perform(post("/api/alunos/" + ALUNO + "/responsaveis").contentType(MediaType.APPLICATION_JSON).content("""
				{ "nome": "", "telefone": "123", "email": "nao-e-email" }
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.erros.nome").exists())
			.andExpect(jsonPath("$.erros.grauParentesco").exists())
			.andExpect(jsonPath("$.erros.telefone").exists())
			.andExpect(jsonPath("$.erros.email").exists());
	}

	@Test
	void naoCadastraResponsavelParaAlunoInexistente() throws Exception {
		mvc.perform(post("/api/alunos/NAO-EXISTE/responsaveis").contentType(MediaType.APPLICATION_JSON).content("""
				{ "nome": "Ana", "grauParentesco": "Mãe", "telefone": "34999991234" }
				"""))
			.andExpect(status().isNotFound());
	}

	@Test
	void listaResponsaveisDoAlunoEContaNoAluno() throws Exception {
		var aluno = alunos.findById(ALUNO).orElseThrow();
		responsaveis.save(new Responsavel(aluno, "Ana", "Mãe", "34999991234", "ana@exemplo.com"));
		responsaveis.save(new Responsavel(aluno, "Carlos", "Pai", "34988887777", null));

		mvc.perform(get("/api/alunos/" + ALUNO + "/responsaveis"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].nome").value("Ana"));

		mvc.perform(get("/api/alunos/" + ALUNO))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Maria da Silva"))
			.andExpect(jsonPath("$.turma.idTurma").value("T-TURMA-1"))
			.andExpect(jsonPath("$.quantidadeResponsaveis").value(2));
	}

	@Test
	void atualizaEExcluiResponsavel() throws Exception {
		var aluno = alunos.findById(ALUNO).orElseThrow();
		var salvo = responsaveis.saveAndFlush(new Responsavel(aluno, "Ana", "Mãe", "34999991234", "ana@exemplo.com"));

		mvc.perform(put("/api/responsaveis/" + salvo.getId()).contentType(MediaType.APPLICATION_JSON).content("""
				{ "nome": "Ana Souza", "grauParentesco": "Tia", "telefone": "+55 34 98888-7777", "email": "" }
				"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Ana Souza"))
			.andExpect(jsonPath("$.grauParentesco").value("Tia"))
			.andExpect(jsonPath("$.telefone").value("5534988887777"))
			.andExpect(jsonPath("$.email").value(nullValue()));

		mvc.perform(delete("/api/responsaveis/" + salvo.getId())).andExpect(status().isNoContent());
		mvc.perform(get("/api/responsaveis/" + salvo.getId())).andExpect(status().isNotFound());
	}

	@Test
	void responsavelInexistenteRetorna404() throws Exception {
		mvc.perform(get("/api/responsaveis/" + UUID.randomUUID())).andExpect(status().isNotFound());
	}

	@Test
	void listaAlunosPorTurma() throws Exception {
		mvc.perform(get("/api/alunos").param("idTurma", "T-TURMA-1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].idDoAluno").value(ALUNO));

		mvc.perform(get("/api/turmas")).andExpect(status().isOk());
	}

}
