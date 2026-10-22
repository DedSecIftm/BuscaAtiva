package io.github.dedseciftm.buscaativa.regra;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Usa o banco do docker-compose; cada teste é desfeito ao final. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegraControllerTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private RegraRepository repository;

	@Test
	void criaRegraNoPeriodo() throws Exception {
		mvc.perform(post("/api/regras").contentType(MediaType.APPLICATION_JSON).content("""
				{ "nome": "  Faltas frequentes ", "tipo": "noPeriodo", "quantidadeFaltas": 3, "periodoEmDias": 10 }
				"""))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.nome").value("Faltas frequentes"))
			.andExpect(jsonPath("$.tipo").value("noPeriodo"))
			.andExpect(jsonPath("$.periodoEmDias").value(10))
			.andExpect(jsonPath("$.ativa").value(true))
			.andExpect(jsonPath("$.criadoEm").isNotEmpty());
	}

	@Test
	void descartaPeriodoEmRegraDeFaltasSeguidas() throws Exception {
		mvc.perform(post("/api/regras").contentType(MediaType.APPLICATION_JSON).content("""
				{ "nome": "Faltas seguidas", "tipo": "seguidas", "quantidadeFaltas": 3, "periodoEmDias": 3 }
				"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.periodoEmDias").value(nullValue()));
	}

	@Test
	void recusaRegraInvalidaComMensagemPorCampo() throws Exception {
		mvc.perform(post("/api/regras").contentType(MediaType.APPLICATION_JSON).content("""
				{ "nome": "", "tipo": "noPeriodo", "quantidadeFaltas": 5, "periodoEmDias": 3 }
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.erros.nome").value("Dê um nome para a regra."))
			.andExpect(jsonPath("$.erros.periodoValido").exists());
	}

	@Test
	void recusaTipoDesconhecido() throws Exception {
		mvc.perform(post("/api/regras").contentType(MediaType.APPLICATION_JSON).content("""
				{ "nome": "X", "tipo": "outro", "quantidadeFaltas": 3 }
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value(startsWith("Os dados enviados")));
	}

	@Test
	void listaAtualizaEExclui() throws Exception {
		var regra = repository.save(new RegraIrregularidade("Faltas seguidas", TipoRegra.SEGUIDAS, 3, null));
		repository.save(new RegraIrregularidade("Faltas frequentes", TipoRegra.NO_PERIODO, 3, 10));

		mvc.perform(get("/api/regras")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

		mvc.perform(put("/api/regras/" + regra.getId()).contentType(MediaType.APPLICATION_JSON).content("""
				{ "nome": "Faltas seguidas", "tipo": "seguidas", "quantidadeFaltas": 5, "ativa": false }
				"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.quantidadeFaltas").value(5))
			.andExpect(jsonPath("$.ativa").value(false));

		mvc.perform(delete("/api/regras/" + regra.getId())).andExpect(status().isNoContent());
		mvc.perform(get("/api/regras/" + regra.getId())).andExpect(status().isNotFound());
	}

	@Test
	void regraInexistenteRetorna404() throws Exception {
		mvc.perform(put("/api/regras/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content("""
				{ "nome": "X", "tipo": "seguidas", "quantidadeFaltas": 3 }
				"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").exists());
	}

}
