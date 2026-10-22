package io.github.dedseciftm.buscaativa.regra;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class RegraIrregularidadeTests {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void regraDeFaltasSeguidasNaoPrecisaDePeriodo() {
		var regra = new RegraIrregularidade("Faltas seguidas", TipoRegra.SEGUIDAS, 3, null);

		assertThat(validator.validate(regra)).isEmpty();
		assertThat(regra.isAtiva()).isTrue();
	}

	@Test
	void regraNoPeriodoValida() {
		var regra = new RegraIrregularidade("Faltas frequentes", TipoRegra.NO_PERIODO, 3, 10);

		assertThat(validator.validate(regra)).isEmpty();
	}

	@Test
	void regraNoPeriodoSemPeriodoEhInvalida() {
		var regra = new RegraIrregularidade("Faltas frequentes", TipoRegra.NO_PERIODO, 3, null);

		assertThat(validator.validate(regra)).hasSize(1);
	}

	@Test
	void periodoMenorQueAsFaltasEhInvalido() {
		var regra = new RegraIrregularidade("Faltas frequentes", TipoRegra.NO_PERIODO, 5, 3);

		assertThat(validator.validate(regra)).hasSize(1);
	}

	@Test
	void nomeEmBrancoEFaltasZeradasSaoInvalidos() {
		var regra = new RegraIrregularidade(" ", TipoRegra.SEGUIDAS, 0, null);

		assertThat(validator.validate(regra)).hasSize(2);
	}

}
