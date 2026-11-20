package io.github.dedseciftm.buscaativa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Turma criada a partir da importação da planilha; o id vem da própria planilha. */
@Entity
@Table(name = "turma")
public class Turma {

	/** Tamanhos das colunas; a importação confere a planilha contra esses limites. */
	public static final int ID_MAX = 12;
	public static final int TIPO_ENSINO_MAX = 15;

	@Id
	@NotBlank
	@Column(length = ID_MAX)
	private String idTurma;

	@NotNull
	@Column(nullable = false)
	private Integer ano;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NivelEscolar nivelEscolar;

	/** Código do tipo de ensino e da turma na planilha, por exemplo "REG 1". */
	@Column(length = TIPO_ENSINO_MAX)
	private String tipoEnsino;

	/** A planilha não informa o período; fica vazio até ser definido. */
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Periodo periodo;

	protected Turma() {
	}

	public Turma(String idTurma, Integer ano, NivelEscolar nivelEscolar, String tipoEnsino, Periodo periodo) {
		this.idTurma = idTurma;
		this.ano = ano;
		this.nivelEscolar = nivelEscolar;
		this.tipoEnsino = tipoEnsino;
		this.periodo = periodo;
	}

	public String getTipoEnsino() {
		return tipoEnsino;
	}

	public void setTipoEnsino(String tipoEnsino) {
		this.tipoEnsino = tipoEnsino;
	}

	public String getIdTurma() {
		return idTurma;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public NivelEscolar getNivelEscolar() {
		return nivelEscolar;
	}

	public void setNivelEscolar(NivelEscolar nivelEscolar) {
		this.nivelEscolar = nivelEscolar;
	}

	public Periodo getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
	}

}
