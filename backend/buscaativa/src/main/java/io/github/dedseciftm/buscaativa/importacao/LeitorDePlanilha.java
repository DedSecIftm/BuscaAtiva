package io.github.dedseciftm.buscaativa.importacao;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dedseciftm.buscaativa.model.Aluno;
import io.github.dedseciftm.buscaativa.model.Turma;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

/**
 * Lê a primeira aba da planilha com as colunas Turma, ID SIMADE, Estudante, % de presença e Faltas.
 * Linhas inválidas não interrompem a leitura: viram erros no resultado.
 */
@Component
public class LeitorDePlanilha {

	public record Resultado(List<LinhaDaPlanilha> linhas, List<String> erros) {
	}

	private static final String TURMA = "turma";
	private static final String ID = "id simade";
	private static final String ESTUDANTE = "estudante";
	private static final String PRESENCA = "% de presenca";
	private static final String FALTAS = "faltas";

	public Resultado ler(InputStream arquivo) throws IOException {
		try (var workbook = abrir(arquivo)) {
			Sheet aba = workbook.getSheetAt(0);
			var formatador = new DataFormatter();

			Map<String, Integer> colunas = null;
			int linhaDoCabecalho = -1;
			for (int i = aba.getFirstRowNum(); i <= Math.min(aba.getLastRowNum(), aba.getFirstRowNum() + 20); i++) {
				colunas = acharColunas(aba.getRow(i), formatador);
				if (colunas != null) {
					linhaDoCabecalho = i;
					break;
				}
			}
			if (colunas == null) {
				throw new PlanilhaInvalidaException(
						"Não encontrei o cabeçalho com as colunas Turma, ID SIMADE, Estudante, % de presença e Faltas.");
			}

			var linhas = new ArrayList<LinhaDaPlanilha>();
			var erros = new ArrayList<String>();
			for (int i = linhaDoCabecalho + 1; i <= aba.getLastRowNum(); i++) {
				Row row = aba.getRow(i);
				if (row == null || vazia(row, formatador)) {
					continue;
				}
				try {
					linhas.add(converter(i + 1, row, colunas, formatador));
				}
				catch (IllegalArgumentException e) {
					erros.add("Linha " + (i + 1) + ": " + e.getMessage());
				}
			}
			return new Resultado(linhas, erros);
		}
	}

	// Arquivo vazio, corrompido ou que não é planilha: o POI lança exceções variadas; para o usuário é tudo "inválido".
	private static Workbook abrir(InputStream arquivo) throws IOException {
		try {
			return WorkbookFactory.create(arquivo);
		}
		catch (IOException | RuntimeException e) {
			throw new PlanilhaInvalidaException("O arquivo não é uma planilha .xlsx válida.");
		}
	}

	private static LinhaDaPlanilha converter(int numero, Row row, Map<String, Integer> colunas, DataFormatter f) {
		String textoTurma = texto(row, colunas.get(TURMA), f);

		var turma = TurmaDaPlanilha.ler(textoTurma)
			.orElseThrow(() -> new IllegalArgumentException("turma em formato inesperado: \"" + textoTurma + "\""));

		if (turma.idTurma().length() > Turma.ID_MAX) {
			throw new IllegalArgumentException("id da turma com mais de " + Turma.ID_MAX + " caracteres");
		}
		if (turma.tipoEnsino().length() > Turma.TIPO_ENSINO_MAX) {
			throw new IllegalArgumentException("tipo de ensino com mais de " + Turma.TIPO_ENSINO_MAX + " caracteres");
		}

		String id = textoDoId(row, colunas.get(ID), f);
		if (id.isEmpty()) {
			throw new IllegalArgumentException("ID SIMADE vazio");
		}
		if (id.length() > Aluno.ID_MAX) {
			throw new IllegalArgumentException("ID SIMADE com mais de " + Aluno.ID_MAX + " caracteres");
		}
		String nome = texto(row, colunas.get(ESTUDANTE), f);
		if (nome.isEmpty()) {
			throw new IllegalArgumentException("nome do estudante vazio");
		}
		if (nome.length() > Aluno.NOME_MAX) {
			throw new IllegalArgumentException("nome do estudante com mais de " + Aluno.NOME_MAX + " caracteres");
		}
		return new LinhaDaPlanilha(numero, turma, id, nome, numero(row, colunas.get(PRESENCA), f, "% de presença"),
				numero(row, colunas.get(FALTAS), f, "faltas").intValue());
	}

	private static Map<String, Integer> acharColunas(Row row, DataFormatter f) {
		if (row == null) {
			return null;
		}
		Map<String, Integer> colunas = new HashMap<>();
		for (Cell cell : row) {
			colunas.put(normalizar(f.formatCellValue(cell)), cell.getColumnIndex());
		}
		return colunas.keySet().containsAll(List.of(TURMA, ID, ESTUDANTE, PRESENCA, FALTAS)) ? colunas : null;
	}

	private static boolean vazia(Row row, DataFormatter f) {
		for (Cell cell : row) {
			if (cell.getCellType() != CellType.BLANK && !f.formatCellValue(cell).isBlank()) {
				return false;
			}
		}
		return true;
	}

	// IDs numéricos vêm como número no Excel; o DataFormatter evita o "6667146.0".
	private static String texto(Row row, int coluna, DataFormatter f) {
		Cell cell = row.getCell(coluna);
		return cell == null ? "" : f.formatCellValue(cell).trim();
	}

	// Número inteiro do Excel vira texto sem notação científica (o formato "Geral" mostraria 1,23457E+16).
	private static String textoDoId(Row row, int coluna, DataFormatter f) {
		Cell cell = row.getCell(coluna);
		if (cell != null && cell.getCellType() == CellType.NUMERIC) {
			double valor = cell.getNumericCellValue();
			if (valor == Math.rint(valor) && !Double.isInfinite(valor)) {
				return new BigDecimal(valor).toBigInteger().toString();
			}
		}
		return texto(row, coluna, f);
	}

	// Aceita número do Excel ou texto com vírgula decimal ("20,93").
	private static BigDecimal numero(Row row, int coluna, DataFormatter f, String campo) {
		Cell cell = row.getCell(coluna);
		if (cell != null && cell.getCellType() == CellType.NUMERIC) {
			return BigDecimal.valueOf(cell.getNumericCellValue());
		}
		String texto = cell == null ? "" : f.formatCellValue(cell).trim().replace("%", "").replace(',', '.');
		try {
			return new BigDecimal(texto);
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(campo + " inválido: \"" + texto + "\"");
		}
	}

	private static String normalizar(String texto) {
		return Normalizer.normalize(texto.trim().toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
	}

}
