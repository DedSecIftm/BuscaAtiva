package io.github.dedseciftm.buscaativa.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Vínculo do aluno com a escola. Os nomes em JSON são os mesmos usados pelo frontend. */
public enum SituacaoAluno {

    @JsonProperty("ativo")
    ATIVO,

    @JsonProperty("trancado")
    TRANCADO,

    @JsonProperty("transferido")
    TRANSFERIDO,

    @JsonProperty("evadido")
    EVADIDO,

    @JsonProperty("concluido")
    CONCLUIDO

}
