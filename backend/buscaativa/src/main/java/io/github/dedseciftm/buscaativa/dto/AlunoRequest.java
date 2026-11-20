package io.github.dedseciftm.buscaativa.dto;

import io.github.dedseciftm.buscaativa.model.SituacaoAluno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados para cadastrar ou editar um aluno. {@code situacao} é opcional: no cadastro vale
 * {@code ativo} e na edição mantém o valor atual.
 */
public record AlunoRequest(
        @NotBlank(message = "O nome não pode ser vazio ou nulo")
        @Size(max = 255, message = "O nome pode ter no máximo 255 caracteres.")
        String nome,

        @NotBlank(message = "A matrícula não pode ser vazia ou nula.")
        @Size(max = 255, message = "A matrícula deve ter no máximo 255 caracteres.")
        String matricula,

        @Email(message = "Informe um e-mail válido.")
        @Size(max = 255, message = "O e-mail pode ter no máximo 255 caracteres.")
        String email,

        @Size(max = 30, message = "O telefone pode ter no máximo 30 caracteres.")
        String telefone,

        SituacaoAluno situacao,

        @NotBlank(message = "Informe a turma do aluno.")
        @Size(max = 100, message = "A turma pode ter no máximo 100 caracteres.")
        String turma

) {
}
