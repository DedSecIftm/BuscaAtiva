package io.github.dedseciftm.buscaativa.aluno;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** Usa o banco do docker-compose; cada teste é desfeito ao final. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "USER")
@Transactional
class AlunoControllerTests {

	private static final String ANA = """
			{ "nome": "  Ana Souza ", "matricula": " 2026001 ", "email": "ana@exemplo.com",
			  "telefone": "(61) 90000-0001", "turma": "6º Ano A" }
			""";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private AlunoRepository repository;

	@Autowired
	private TurmaRepository turmaRepository;

	@Autowired
	private ResponsavelRepository responsavelRepository;

	@BeforeEach
	void limpa() {
		repository.deleteAll();
		turmaRepository.deleteAll();
		responsavelRepository.deleteAll();
	}

	private ResultActions enviar(String corpo) throws Exception {
		return mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON).content(corpo));
	}

	private ResultActions importar(String corpo) throws Exception {
		return mvc.perform(post("/api/alunos/importacao").contentType(MediaType.APPLICATION_JSON).content(corpo));
	}

	private String cadastrar(String corpo) throws Exception {
		var resposta = enviar(corpo).andExpect(status().isCreated()).andReturn().getResponse();
		return JsonPath.read(resposta.getContentAsString(), "$.id");
	}

	@Test
	void cadastraAlunoEPersisteNoBanco() throws Exception {
		enviar(ANA)
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", startsWith("/api/alunos/")))
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.nome").value("Ana Souza"))
			.andExpect(jsonPath("$.matricula").value("2026001"))
			.andExpect(jsonPath("$.situacao").value("ativo"))
			.andExpect(jsonPath("$.turma").value("6º Ano A"))
			.andExpect(jsonPath("$.criadoEm").isNotEmpty());

		assertThat(repository.findByMatricula("2026001")).isPresent();
		assertThat(turmaRepository.findByNomeIgnoreCase("6º ano a")).isPresent();
	}

	@Test
	void recusaAlunoInvalidoComMensagemPorCampo() throws Exception {
		enviar("""
				{ "nome": "", "matricula": "", "email": "invalido", "turma": "" }
				""")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.erros.nome").exists())
			.andExpect(jsonPath("$.erros.matricula").exists())
			.andExpect(jsonPath("$.erros.email").value("Informe um e-mail válido."))
			.andExpect(jsonPath("$.erros.turma").value("Informe a turma do aluno."));
	}

	@Test
	void recusaMatriculaRepetida() throws Exception {
		cadastrar(ANA);

		enviar(ANA)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.erros.matricula").exists());
	}

	@Test
	void consultaEEditaAluno() throws Exception {
		var id = cadastrar(ANA);
		cadastrar("""
				{ "nome": "Bruno Lima", "matricula": "2026002", "turma": "6º Ano A" }
				""");

		mvc.perform(get("/api/alunos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].nome").value("Ana Souza"));

		mvc.perform(put("/api/alunos/" + id).contentType(MediaType.APPLICATION_JSON).content("""
				{ "nome": "Ana Maria Souza", "matricula": "2026001", "situacao": "transferido", "turma": "7º Ano B" }
				"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.situacao").value("transferido"))
			.andExpect(jsonPath("$.turma").value("7º Ano B"));

		mvc.perform(get("/api/alunos/" + id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Ana Maria Souza"))
			.andExpect(jsonPath("$.email").isEmpty());
	}

	@Test
	void naoDeixaEditarParaMatriculaDeOutroAluno() throws Exception {
		var id = cadastrar(ANA);
		cadastrar("""
				{ "nome": "Bruno Lima", "matricula": "2026002", "turma": "6º Ano A" }
				""");

		mvc.perform(put("/api/alunos/" + id).contentType(MediaType.APPLICATION_JSON).content("""
				{ "nome": "Ana Souza", "matricula": "2026002", "turma": "6º Ano A" }
				"""))
			.andExpect(status().isConflict());
	}

	@Test
	void exclusaoLogicaEscondeOAlunoMasMantemORegistro() throws Exception {
		var id = cadastrar(ANA);

		mvc.perform(delete("/api/alunos/" + id)).andExpect(status().isNoContent());

		mvc.perform(get("/api/alunos")).andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(get("/api/alunos/" + id)).andExpect(status().isNotFound());
		mvc.perform(delete("/api/alunos/" + id)).andExpect(status().isNotFound());
		assertThat(repository.findById(UUID.fromString(id))).get()
			.satisfies((aluno) -> assertThat(aluno.getExcluidoEm()).isNotNull());
	}

	@Test
	void recadastrarMatriculaExcluidaTrazOAlunoDeVolta() throws Exception {
		var id = cadastrar(ANA);
		mvc.perform(delete("/api/alunos/" + id)).andExpect(status().isNoContent());

		enviar(ANA)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(id));
	}

	@Test
	void importaArquivoSemMatriculaEReimportarNaoDuplica() throws Exception {
		var arquivo = """
				{ "alunos": [
				  { "nome": "Yasmin Freitas", "turma": "8º Ano C", "telefone": "(61) 90000-0025", "faltas": "0", "periodoEmDias": "30" },
				  { "nome": "Arthur Monteiro", "turma": "8º Ano C", "telefone": "(61) 90000-0026", "faltas": "2", "periodoEmDias": "30" }
				] }
				""";

		importar(arquivo)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.criados").value(2))
			.andExpect(jsonPath("$.atualizados").value(0))
			.andExpect(jsonPath("$.erros", hasSize(0)));

		importar(arquivo.replace("\"faltas\": \"2\"", "\"faltas\": \"7\"").replace("Arthur Monteiro", "ARTHUR  monteiro"))
			.andExpect(jsonPath("$.criados").value(0))
			.andExpect(jsonPath("$.atualizados").value(2));

		mvc.perform(get("/api/alunos"))
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].faltas").value(7))
			.andExpect(jsonPath("$[1].telefone").value("(61) 90000-0025"));
		assertThat(turmaRepository.count()).isEqualTo(1);
	}

	@Test
	void importacaoGravaAsLinhasValidasEInformaAsInvalidas() throws Exception {
		importar("""
				{ "alunos": [
				  { "nome": "Válido", "turma": "8º Ano C" },
				  { "nome": "", "turma": "8º Ano C" },
				  { "nome": "Sem turma" },
				  { "nome": "Faltas erradas", "turma": "8º Ano C", "faltas": "muitas" },
				  { "nome": "Período zero", "turma": "8º Ano C", "periodoEmDias": "0" },
				  { "nome": "E-mail errado", "turma": "8º Ano C", "email": "x" }
				] }
				""")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.criados").value(1))
			.andExpect(jsonPath("$.erros", hasSize(5)))
			.andExpect(jsonPath("$.erros[0].linha").value(2))
			.andExpect(jsonPath("$.erros[0].mensagem").value("Nome não informado."))
			.andExpect(jsonPath("$.erros[1].mensagem").value("Turma não informada."));

		mvc.perform(get("/api/alunos")).andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void importacaoReconheceAlunoPelaMatriculaEMantemCamposAusentes() throws Exception {
		var id = cadastrar(ANA);

		importar("""
				{ "alunos": [ { "nome": "Ana Souza", "matricula": "2026001", "turma": "7º Ano B", "faltas": "4" } ] }
				""")
			.andExpect(jsonPath("$.atualizados").value(1));

		mvc.perform(get("/api/alunos/" + id))
			.andExpect(jsonPath("$.turma").value("7º Ano B"))
			.andExpect(jsonPath("$.faltas").value(4))
			.andExpect(jsonPath("$.email").value("ana@exemplo.com"));
	}

	@Test
	void importacaoCompletaMatriculaDeAlunoJaImportadoSemEla() throws Exception {
		importar("""
				{ "alunos": [ { "nome": "Enzo Batista", "turma": "8º Ano C" } ] }
				""");

		importar("""
				{ "alunos": [ { "nome": "Enzo Batista", "turma": "8º Ano C", "matricula": "2026010" } ] }
				""")
			.andExpect(jsonPath("$.criados").value(0))
			.andExpect(jsonPath("$.atualizados").value(1));

		assertThat(repository.findByMatricula("2026010")).isPresent();
	}

	@Test
	void homonimosComMatriculasDiferentesSaoAlunosDiferentes() throws Exception {
		importar("""
				{ "alunos": [
				  { "nome": "João Silva", "matricula": "1", "turma": "8º Ano C" },
				  { "nome": "João Silva", "matricula": "2", "turma": "8º Ano C" }
				] }
				""")
			.andExpect(jsonPath("$.criados").value(2));
	}

	@Test
	void registroRepetidoNoArquivoNaoDuplicaOAluno() throws Exception {
		importar("""
				{ "alunos": [
				  { "nome": "Enzo Batista", "matricula": "7", "turma": "8º Ano C" },
				  { "nome": "Enzo Batista", "matricula": "7", "turma": "8º Ano C" }
				] }
				""")
			.andExpect(jsonPath("$.criados").value(1))
			.andExpect(jsonPath("$.atualizados").value(1));

		mvc.perform(get("/api/alunos")).andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void importacaoTrazDeVoltaAlunoExcluidoQueReaparece() throws Exception {
		var id = cadastrar(ANA);
		mvc.perform(delete("/api/alunos/" + id)).andExpect(status().isNoContent());

		importar("""
				{ "alunos": [ { "nome": "Ana Souza", "matricula": "2026001", "turma": "6º Ano A" } ] }
				""")
			.andExpect(jsonPath("$.atualizados").value(1));

		mvc.perform(get("/api/alunos/" + id)).andExpect(status().isOk());
	}

	@Test
	void irmaosDividemOMesmoResponsavel() throws Exception {
		importar("""
				{ "alunos": [
				  { "nome": "Lia Rocha", "turma": "6º Ano A", "responsavelNome": "Rita Rocha",
				    "responsavelEmail": "Rita@Exemplo.com", "responsavelParentesco": "Mãe" },
				  { "nome": "Leo Rocha", "turma": "7º Ano B", "responsavelNome": "Rita Rocha",
				    "responsavelEmail": "rita@exemplo.com", "responsavelParentesco": "Mãe" }
				] }
				""")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.criados").value(2))
			.andExpect(jsonPath("$.responsaveisCriados").value(1))
			.andExpect(jsonPath("$.responsaveisVinculados").value(2));

		mvc.perform(get("/api/alunos"))
			.andExpect(jsonPath("$[0].responsaveis", hasSize(1)))
			.andExpect(jsonPath("$[0].responsaveis[0].email").value("rita@exemplo.com"))
			.andExpect(jsonPath("$[0].responsaveis[0].parentesco").value("mae"))
			.andExpect(jsonPath("$[1].responsaveis[0].id").isNotEmpty());
		assertThat(responsavelRepository.count()).isEqualTo(1);
	}

	@Test
	void reconheceResponsavelPeloTelefoneEmQualquerFormato() throws Exception {
		importar("""
				{ "alunos": [ { "nome": "Lia Rocha", "turma": "6º Ano A", "responsavelNome": "Rita Rocha",
				  "responsavelTelefone": "(61) 90000-1234", "responsavelParentesco": "tia" } ] }
				""")
			.andExpect(jsonPath("$.responsaveisCriados").value(1));

		importar("""
				{ "alunos": [ { "nome": "Leo Rocha", "turma": "6º Ano A", "responsavelNome": "Rita R.",
				  "responsavelTelefone": "61 900001234", "responsavelEmail": "rita@exemplo.com" } ] }
				""")
			.andExpect(jsonPath("$.responsaveisCriados").value(0))
			.andExpect(jsonPath("$.responsaveisVinculados").value(1));

		// Campo vazio é completado e o nome já gravado não é sobrescrito.
		mvc.perform(get("/api/alunos"))
			.andExpect(jsonPath("$[0].responsaveis[0].nome").value("Rita Rocha"))
			.andExpect(jsonPath("$[0].responsaveis[0].telefone").value("61900001234"))
			.andExpect(jsonPath("$[0].responsaveis[0].email").value("rita@exemplo.com"));
	}

	@Test
	void parentescoPertenceAoVinculoEntreAlunoEResponsavel() throws Exception {
		importar("""
				{ "alunos": [
				  { "nome": "Ana Rocha", "turma": "6º Ano A", "responsavelNome": "Rita Rocha",
				    "responsavelEmail": "rita@exemplo.com", "responsavelParentesco": "Mãe" },
				  { "nome": "Bia Lima", "turma": "6º Ano A", "responsavelNome": "Rita Rocha",
				    "responsavelEmail": "rita@exemplo.com", "responsavelParentesco": "Tia" }
				] }
				""")
			.andExpect(jsonPath("$.responsaveisCriados").value(1))
			.andExpect(jsonPath("$.responsaveisVinculados").value(2));

		mvc.perform(get("/api/alunos"))
			.andExpect(jsonPath("$[0].responsaveis[0].parentesco").value("mae"))
			.andExpect(jsonPath("$[1].responsaveis[0].parentesco").value("tio"));
	}

	@Test
	void reimportarAtualizaOParentescoSemCriarVinculo() throws Exception {
		var linha = """
				{ "alunos": [ { "nome": "Ana Rocha", "turma": "6º Ano A", "responsavelNome": "Rita Rocha",
				  "responsavelEmail": "rita@exemplo.com", "responsavelParentesco": "%s" } ] }
				""";
		importar(linha.formatted("Tia"));

		importar(linha.formatted("Tutora"))
			.andExpect(jsonPath("$.responsaveisVinculados").value(0));
		mvc.perform(get("/api/alunos")).andExpect(jsonPath("$[0].responsaveis[0].parentesco").value("tutor"));

		importar(linha.formatted(""))
			.andExpect(jsonPath("$.responsaveisVinculados").value(0));
		mvc.perform(get("/api/alunos"))
			.andExpect(jsonPath("$[0].responsaveis", hasSize(1)))
			.andExpect(jsonPath("$[0].responsaveis[0].parentesco").value("tutor"));
	}

	@Test
	void responsavelSemParentescoInformadoFicaComoOutro() throws Exception {
		importar("""
				{ "alunos": [ { "nome": "Ana Rocha", "turma": "6º Ano A", "responsavelNome": "Rita Rocha",
				  "responsavelEmail": "rita@exemplo.com" } ] }
				""");

		mvc.perform(get("/api/alunos")).andExpect(jsonPath("$[0].responsaveis[0].parentesco").value("outro"));
	}

	@Test
	void alunoRepetidoNoArquivoGanhaOutroResponsavel() throws Exception {
		var arquivo = """
				{ "alunos": [
				  { "nome": "Lia Rocha", "matricula": "10", "turma": "6º Ano A", "responsavelNome": "Rita Rocha",
				    "responsavelEmail": "rita@exemplo.com", "responsavelParentesco": "mae" },
				  { "nome": "Lia Rocha", "matricula": "10", "turma": "6º Ano A", "responsavelNome": "Caio Rocha",
				    "responsavelEmail": "caio@exemplo.com", "responsavelParentesco": "pai" }
				] }
				""";

		importar(arquivo)
			.andExpect(jsonPath("$.criados").value(1))
			.andExpect(jsonPath("$.atualizados").value(1))
			.andExpect(jsonPath("$.responsaveisCriados").value(2))
			.andExpect(jsonPath("$.responsaveisVinculados").value(2));

		importar(arquivo)
			.andExpect(jsonPath("$.responsaveisCriados").value(0))
			.andExpect(jsonPath("$.responsaveisVinculados").value(0));

		mvc.perform(get("/api/alunos"))
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].responsaveis", hasSize(2)))
			.andExpect(jsonPath("$[0].responsaveis[0].nome").value("Caio Rocha"))
			.andExpect(jsonPath("$[0].responsaveis[1].parentesco").value("mae"));
	}

	@Test
	void reimportarMantemResponsaveisQueNaoVieramNoArquivo() throws Exception {
		importar("""
				{ "alunos": [ { "nome": "Lia Rocha", "turma": "6º Ano A", "responsavelNome": "Rita Rocha",
				  "responsavelEmail": "rita@exemplo.com" } ] }
				""");

		importar("""
				{ "alunos": [ { "nome": "Lia Rocha", "turma": "6º Ano A", "faltas": "2" } ] }
				""")
			.andExpect(jsonPath("$.atualizados").value(1));

		mvc.perform(get("/api/alunos")).andExpect(jsonPath("$[0].responsaveis", hasSize(1)));
	}

	@Test
	void parentescoForaDaListaViraOutro() throws Exception {
		importar("""
				{ "alunos": [ { "nome": "Lia Rocha", "turma": "6º Ano A", "responsavelNome": "Vera Rocha",
				  "responsavelTelefone": "61900001234", "responsavelParentesco": "Avó" } ] }
				""");

		mvc.perform(get("/api/alunos"))
			.andExpect(jsonPath("$[0].responsaveis[0].parentesco").value("outro"));
	}

	@Test
	void responsavelIncompletoGeraErroNaLinhaSemGravarOAluno() throws Exception {
		importar("""
				{ "alunos": [
				  { "nome": "Sem contato", "turma": "6º Ano A", "responsavelNome": "Rita Rocha" },
				  { "nome": "Sem nome", "turma": "6º Ano A", "responsavelEmail": "rita@exemplo.com" },
				  { "nome": "E-mail errado", "turma": "6º Ano A", "responsavelNome": "Rita", "responsavelEmail": "x" },
				  { "nome": "Sem responsável", "turma": "6º Ano A" }
				] }
				""")
			.andExpect(jsonPath("$.criados").value(1))
			.andExpect(jsonPath("$.erros", hasSize(3)))
			.andExpect(jsonPath("$.erros[0].mensagem")
				.value("Informe o nome e um contato (e-mail ou telefone) do responsável."));

		assertThat(responsavelRepository.count()).isZero();
	}

	@Test
	void importaMesmoComTurmasQueSoDiferemEmEspacosNaBase() throws Exception {
		turmaRepository.saveAndFlush(new Turma("6º  Ano A"));
		turmaRepository.saveAndFlush(new Turma("6º Ano A"));

		importar("""
				{ "alunos": [ { "nome": "Ana Rocha", "turma": "6º Ano A" } ] }
				""")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.criados").value(1));
	}

	@Test
	void naoCriaTurmaNovaPorCausaDeEspacosRepetidos() throws Exception {
		cadastrar("""
				{ "nome": "Ana Rocha", "matricula": "1", "turma": "6º  Ano  A" }
				""");
		importar("""
				{ "alunos": [ { "nome": "Bia Lima", "turma": "6º   Ano A" } ] }
				""");

		assertThat(turmaRepository.count()).isEqualTo(1);
		assertThat(turmaRepository.findByNomeIgnoreCase("6º Ano A")).isPresent();
	}

	@Test
	void recusaImportacaoVazia() throws Exception {
		importar("""
				{ "alunos": [] }
				""")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.erros.alunos").value("Nenhum registro foi enviado."));
	}

	@Test
	@WithAnonymousUser
	void exigeLogin() throws Exception {
		mvc.perform(get("/api/alunos")).andExpect(status().isUnauthorized());
		importar("{ \"alunos\": [] }").andExpect(status().isUnauthorized());
	}

}
