package io.github.dedseciftm.buscaativa.aluno;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Dados de alunos e responsáveis são pessoais: nenhum endpoint pode responder sem login. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcessoAosDadosDeAlunosTests {

	@Autowired
	private MockMvc mvc;

	@Test
	void todosOsEndpointsExigemLogin() throws Exception {
		var corpo = """
				{ "nome": "Ana", "grauParentesco": "Mãe", "telefone": "34999991234" }
				""";
		var id = "00000000-0000-0000-0000-000000000000";

		mvc.perform(get("/api/turmas")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/alunos")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/alunos/1")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/alunos/1/responsaveis")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/alunos/1/responsaveis").contentType(MediaType.APPLICATION_JSON).content(corpo))
			.andExpect(status().isUnauthorized());
		mvc.perform(get("/api/responsaveis")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/responsaveis/" + id)).andExpect(status().isUnauthorized());
		mvc.perform(put("/api/responsaveis/" + id).contentType(MediaType.APPLICATION_JSON).content(corpo))
			.andExpect(status().isUnauthorized());
		mvc.perform(delete("/api/responsaveis/" + id)).andExpect(status().isUnauthorized());
		mvc.perform(multipart("/api/importacoes")
				.file(new MockMultipartFile("arquivo", "a.xlsx", "application/octet-stream", new byte[] { 1 })))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void tokenInvalidoTambemEhRecusado() throws Exception {
		mvc.perform(get("/api/alunos").header("Authorization", "Bearer token-que-nao-existe"))
			.andExpect(status().isUnauthorized());
	}

}
