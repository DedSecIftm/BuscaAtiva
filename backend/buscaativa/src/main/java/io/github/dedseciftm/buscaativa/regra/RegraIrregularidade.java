package io.github.dedseciftm.buscaativa.regra;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Situação de frequência considerada irregular, por exemplo "3 faltas seguidas"
 * ou "3 faltas dentro de 10 dias".
 */
@Entity
@Table(name = "regra_irregularidade")
public class RegraIrregularidade {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String nome;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoRegra tipo;

	@NotNull
	@Min(1)
	@Column(nullable = false)
	private Integer quantidadeFaltas;

	/** Usado apenas quando o tipo é {@link TipoRegra#NO_PERIODO}. */
	@Min(1)
	private Integer periodoEmDias;

	@Column(nullable = false)
	private boolean ativa = true;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant criadoEm;

	protected RegraIrregularidade() {
	}

	public RegraIrregularidade(String nome, TipoRegra tipo, Integer quantidadeFaltas, Integer periodoEmDias) {
		this.nome = nome;
		this.tipo = tipo;
		this.quantidadeFaltas = quantidadeFaltas;
		this.periodoEmDias = periodoEmDias;
	}

	@AssertTrue(message = "o período deve ser informado e ter pelo menos a mesma quantidade de dias que as faltas")
	private boolean isPeriodoValido() {
		return periodoValido(tipo, quantidadeFaltas, periodoEmDias);
	}

	/** Numa regra {@link TipoRegra#NO_PERIODO}, o período precisa caber a quantidade de faltas. */
	public static boolean periodoValido(TipoRegra tipo, Integer quantidadeFaltas, Integer periodoEmDias) {
		if (tipo != TipoRegra.NO_PERIODO) {
			return true;
		}
		return periodoEmDias != null && quantidadeFaltas != null && periodoEmDias >= quantidadeFaltas;
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public TipoRegra getTipo() {
		return tipo;
	}

	public void setTipo(TipoRegra tipo) {
		this.tipo = tipo;
	}

	public Integer getQuantidadeFaltas() {
		return quantidadeFaltas;
	}

	public void setQuantidadeFaltas(Integer quantidadeFaltas) {
		this.quantidadeFaltas = quantidadeFaltas;
	}

	public Integer getPeriodoEmDias() {
		return periodoEmDias;
	}

	public void setPeriodoEmDias(Integer periodoEmDias) {
		this.periodoEmDias = periodoEmDias;
	}

	public boolean isAtiva() {
		return ativa;
	}

	public void setAtiva(boolean ativa) {
		this.ativa = ativa;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

}
