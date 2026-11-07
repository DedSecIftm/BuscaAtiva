package io.github.dedseciftm.buscaativa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Vínculo entre um aluno e um responsável. O parentesco fica aqui porque pertence à relação: a mesma pessoa
 * pode ser mãe de um aluno e tia de outro.
 */
@Entity
@Table(name = "aluno_responsavel",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "responsavel_id"}))
@Getter
@Setter
@NoArgsConstructor
public class AlunoResponsavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Responsavel responsavel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Parentesco parentesco = Parentesco.OUTRO;

    public AlunoResponsavel(Aluno aluno, Responsavel responsavel, Parentesco parentesco) {
        this.aluno = aluno;
        this.responsavel = responsavel;
        this.parentesco = parentesco;
    }
}
