package io.github.dedseciftm.buscaativa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Sem {@code @Data} e sem {@code toString}/{@code equals} gerados: com o relacionamento nos dois lados,
 * as entidades chamariam umas às outras até estourar a pilha.
 */
@Entity
@Table(name = "responsavel")
@Getter
@Setter
public class Responsavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String nome;

    /** Só os dígitos, para o mesmo número ser reconhecido em qualquer formato. */
    @Size(max = 30)
    @Column(length = 30)
    private String telefone;

    /** Em minúsculas, pelo mesmo motivo do telefone. */
    @Email
    @Size(max = 255)
    @Column(length = 255)
    private String email;

    @OneToMany(mappedBy = "responsavel")
    private Set<AlunoResponsavel> alunos = new HashSet<>();
}
