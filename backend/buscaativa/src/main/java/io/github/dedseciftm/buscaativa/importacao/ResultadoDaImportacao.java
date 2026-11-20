package io.github.dedseciftm.buscaativa.importacao;

import java.util.List;

public record ResultadoDaImportacao(int turmasCriadas, int alunosCriados, int alunosAtualizados,
		int linhasComErro, List<String> erros) {
}
