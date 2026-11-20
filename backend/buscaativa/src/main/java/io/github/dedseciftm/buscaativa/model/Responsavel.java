package io.github.dedseciftm.buscaativa.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Responsável por um aluno; cadastrado manualmente pelo usuário. */
@Entity
@Table(name = "responsavel")
public class Responsavel {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_aluno", nullable = false)
	private Aluno aluno;

	@Column(nullable = false, length = 100)
	private String nome;

	@Column(nullable = false, length = 20)
	private String grauParentesco;

	/** Somente dígitos, com DDD (e DDI, se informado). */
	@Column(nullable = false, length = 20)
	private String telefone;

	@Column(length = 80)
	private String email;

	protected Responsavel() {
	}

	public Responsavel(Aluno aluno, String nome, String grauParentesco, String telefone, String email) {
		this.aluno = aluno;
		this.nome = nome;
		this.grauParentesco = grauParentesco;
		this.telefone = telefone;
		this.email = email;
	}

	public UUID getId() {
		return id;
	}

	public Aluno getAluno() {
		return aluno;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getGrauParentesco() {
		return grauParentesco;
	}

	public void setGrauParentesco(String grauParentesco) {
		this.grauParentesco = grauParentesco;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
