package io.github.dedseciftm.buscaativa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "aluno")
@Getter
@Setter
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(min = 1, max = 255)
    @Column(nullable = false, length = 255)
    private String nome;

    /** Obrigatória no cadastro manual; pode ficar vazia em alunos importados de arquivos sem essa coluna. */
    @Size(max = 255)
    @Column(unique = true, length = 255)
    private String matricula;

    @Email
    @Size(max = 255)
    @Column(length = 255)
    private String email;

    @Size(max = 30)
    @Column(length = 30)
    private String telefone;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoAluno situacao = SituacaoAluno.ATIVO;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @OneToMany(mappedBy = "aluno", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private Set<AlunoResponsavel> responsaveis = new HashSet<>();

    /** Dados do último arquivo importado. */
    @Min(0)
    private Integer faltas;

    @Min(1)
    private Integer periodoEmDias;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    /** Exclusão lógica: quando preenchido, o aluno não aparece mais nas consultas. */
    private Instant excluidoEm;

    /**
     * Cria o vínculo com o responsável ou atualiza o parentesco do vínculo existente; sem parentesco informado,
     * mantém o atual e, em vínculo novo, usa OUTRO.
     *
     * @return se um vínculo novo foi criado
     */
    public boolean vincular(Responsavel responsavel, Parentesco parentesco) {
        for (var vinculo : responsaveis) {
            if (vinculo.getResponsavel().getId().equals(responsavel.getId())) {
                if (parentesco != null) {
                    vinculo.setParentesco(parentesco);
                }
                return false;
            }
        }
        var vinculo = new AlunoResponsavel(this, responsavel, parentesco != null ? parentesco : Parentesco.OUTRO);
        return responsaveis.add(vinculo);
    }
}
