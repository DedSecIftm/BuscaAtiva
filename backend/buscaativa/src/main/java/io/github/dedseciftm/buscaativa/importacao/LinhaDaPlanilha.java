package io.github.dedseciftm.buscaativa.importacao;

import java.math.BigDecimal;

/** Uma linha de estudante da planilha, já convertida para tipos do sistema. */
public record LinhaDaPlanilha(int numeroDaLinha, TurmaDaPlanilha turma, String idDoAluno, String nome,
		BigDecimal percentualPresenca, int faltas) {
}
