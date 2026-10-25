package io.github.dedseciftm.buscaativa.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Os nomes em JSON são os mesmos usados pelo frontend. */
public enum TipoRegra {

	/** Faltas em dias letivos consecutivos. */
	@JsonProperty("seguidas")
	SEGUIDAS,

	/** Faltas seguidas ou intercaladas dentro de um período de dias. */
	@JsonProperty("noPeriodo")
	NO_PERIODO

}
