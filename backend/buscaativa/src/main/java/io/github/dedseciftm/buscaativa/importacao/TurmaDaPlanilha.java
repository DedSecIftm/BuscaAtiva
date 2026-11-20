package io.github.dedseciftm.buscaativa.importacao;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import io.github.dedseciftm.buscaativa.model.NivelEscolar;

/**
 * Dados lidos da coluna "Turma" da planilha, que é uma string única como
 * {@code 1º EM REG 1 - 2079040}: ano, nível escolar, tipo de ensino e id da turma.
 */
public record TurmaDaPlanilha(String idTurma, int ano, NivelEscolar nivelEscolar, String tipoEnsino) {

	private static final Pattern FORMATO = Pattern
		.compile("^(\\d+)\\s*[º°o]?\\s+(EF|EM)\\s+(.+?)\\s*-\\s*(\\d+)$", Pattern.CASE_INSENSITIVE);

	/** Retorna vazio se o texto não seguir o formato esperado. */
	public static Optional<TurmaDaPlanilha> ler(String texto) {
		if (texto == null) {
			return Optional.empty();
		}
		var m = FORMATO.matcher(texto.trim().replaceAll("\\s+", " "));
		if (!m.matches()) {
			return Optional.empty();
		}
		var nivel = m.group(2).toUpperCase(Locale.ROOT).equals("EF") ? NivelEscolar.FUNDAMENTAL : NivelEscolar.MEDIO;
		return Optional.of(new TurmaDaPlanilha(m.group(4), Integer.parseInt(m.group(1)), nivel, m.group(3).trim()));
	}

}
