package io.github.dedseciftm.buscaativa.importacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.github.dedseciftm.buscaativa.model.NivelEscolar;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/** Não usa banco nem Spring. */
class LeitorDePlanilhaTests {

	private final LeitorDePlanilha leitor = new LeitorDePlanilha();

	@Test
	void leTurmaDoExemploDaPlanilha() {
		var turma = TurmaDaPlanilha.ler("1º EM REG 1 - 2079040").orElseThrow();

		assertEquals("2079040", turma.idTurma());
		assertEquals(1, turma.ano());
		assertEquals(NivelEscolar.MEDIO, turma.nivelEscolar());
		assertEquals("REG 1", turma.tipoEnsino());
	}

	@Test
	void leTurmaDoFundamental() {
		var turma = TurmaDaPlanilha.ler(" 9º  EF  REG 2 -  123 ").orElseThrow();

		assertEquals(9, turma.ano());
		assertEquals(NivelEscolar.FUNDAMENTAL, turma.nivelEscolar());
		assertEquals("123", turma.idTurma());
	}

	@Test
	void rejeitaTurmaForaDoFormato() {
		assertTrue(TurmaDaPlanilha.ler("Turma qualquer").isEmpty());
		assertTrue(TurmaDaPlanilha.ler(null).isEmpty());
	}

	@Test
	void leLinhasEAcumulaErros() throws IOException {
		var resultado = leitor.ler(planilha(
				new Object[] { "Relatório de frequência" },
				new Object[] { "Turma", "ID SIMADE", "Estudante", "% de presença", "Faltas" },
				new Object[] { "1º EM REG 1 - 2079040", 6667146d, "Kawan Eustáquio Silva", "20,93", 68d },
				new Object[] { "texto solto", 1d, "Fulano", 90d, 2d },
				new Object[] {}));

		assertEquals(1, resultado.linhas().size());
		var linha = resultado.linhas().get(0);
		assertEquals("6667146", linha.idDoAluno());
		assertEquals("Kawan Eustáquio Silva", linha.nome());
		assertEquals(0, new java.math.BigDecimal("20.93").compareTo(linha.percentualPresenca()));
		assertEquals(68, linha.faltas());
		assertEquals(1, resultado.erros().size());
		assertTrue(resultado.erros().get(0).startsWith("Linha 4"));
	}

	@Test
	void rejeitaPlanilhaSemCabecalho() throws IOException {
		var arquivo = planilha(new Object[] { "a", "b" });

		assertThrows(PlanilhaInvalidaException.class, () -> leitor.ler(arquivo));
	}

	private static ByteArrayInputStream planilha(Object[]... linhas) throws IOException {
		try (var wb = new XSSFWorkbook(); var saida = new ByteArrayOutputStream()) {
			var aba = wb.createSheet();
			for (int i = 0; i < linhas.length; i++) {
				var row = aba.createRow(i);
				for (int j = 0; j < linhas[i].length; j++) {
					if (linhas[i][j] instanceof Double d) {
						row.createCell(j).setCellValue(d);
					}
					else {
						row.createCell(j).setCellValue(String.valueOf(linhas[i][j]));
					}
				}
			}
			wb.write(saida);
			return new ByteArrayInputStream(saida.toByteArray());
		}
	}

}
