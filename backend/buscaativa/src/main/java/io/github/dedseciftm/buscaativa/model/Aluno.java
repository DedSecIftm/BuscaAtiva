package io.github.dedseciftm.buscaativa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Aluno criado a partir da importação da planilha; não é cadastrado manualmente. */
@Entity
@Table(name = "aluno")
public class Aluno {

	@Id
	@NotBlank
	@Column(length = 16)
	private String idDoAluno;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_turma", nullable = false)
	private Turma turma;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String nome;

	protected Aluno() {
	}

	public Aluno(String idDoAluno, Turma turma, String nome) {
		this.idDoAluno = idDoAluno;
		this.turma = turma;
		this.nome = nome;
	}

	public String getIdDoAluno() {
		return idDoAluno;
	}

	public Turma getTurma() {
		return turma;
	}

	public void setTurma(Turma turma) {
		this.turma = turma;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

}
