package io.github.dedseciftm.buscaativa.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Os nomes em JSON são os mesmos usados pelo frontend. */
public enum Parentesco {

    @JsonProperty("pai")
    PAI,

    @JsonProperty("mae")
    MAE,

    @JsonProperty("tutor")
    TUTOR,

    @JsonProperty("tio")
    TIO,

    @JsonProperty("outro")
    OUTRO

}
