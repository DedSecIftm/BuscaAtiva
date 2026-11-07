package io.github.dedseciftm.buscaativa.dto;

import java.util.List;

/** Em {@code erros}, {@code linha} é a posição do registro no arquivo, começando em 1. */
public record ImportacaoAlunosResponse(int criados, int atualizados, List<ErroDeLinha> erros) {

    public record ErroDeLinha(int linha, String mensagem) {
    }

}
