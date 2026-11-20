package io.github.dedseciftm.buscaativa.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Registros de um arquivo importado. As linhas não são validadas aqui (sem {@code @Valid}), e sim uma a
 * uma no serviço: assim um registro com problema é informado sem impedir a importação dos demais.
 */
public record ImportacaoAlunosRequest(
        @NotEmpty(message = "Nenhum registro foi enviado.")
        @Size(max = 10000, message = "Envie no máximo 10.000 registros por arquivo.")
        List<@NotNull(message = "Registro vazio.") Linha> alunos) {

    /** Tudo é texto porque vem de um arquivo; os campos que não estiverem no arquivo chegam vazios. */
    public record Linha(
            @NotBlank(message = "Nome não informado.")
            @Size(max = 255, message = "O nome pode ter no máximo 255 caracteres.")
            String nome,

            @Size(max = 255, message = "A matrícula deve ter no máximo 255 caracteres.")
            String matricula,

            @Email(message = "E-mail inválido.")
            @Size(max = 255, message = "O e-mail pode ter no máximo 255 caracteres.")
            String email,

            @Size(max = 30, message = "O telefone pode ter no máximo 30 caracteres.")
            String telefone,

            @NotBlank(message = "Turma não informada.")
            @Size(max = 100, message = "A turma pode ter no máximo 100 caracteres.")
            String turma,

            @Pattern(regexp = "\\d{0,6}", message = "As faltas devem ser um número inteiro.")
            String faltas,

            @Pattern(regexp = "|[1-9]\\d{0,5}", message = "O período deve ser um número de dias maior que zero.")
            String periodoEmDias,

            @Size(max = 255, message = "O nome do responsável pode ter no máximo 255 caracteres.")
            String responsavelNome,

            @Size(max = 30, message = "O telefone do responsável pode ter no máximo 30 caracteres.")
            String responsavelTelefone,

            @Email(message = "E-mail do responsável inválido.")
            @Size(max = 255, message = "O e-mail do responsável pode ter no máximo 255 caracteres.")
            String responsavelEmail,

            String responsavelParentesco) {

        public boolean temResponsavel() {
            return preenchido(responsavelNome) || preenchido(responsavelEmail) || preenchido(responsavelParentesco)
                    || preenchido(responsavelTelefone);
        }

        /** O contato é o que reconhece o responsável em outras linhas; o responsável inteiro é opcional. */
        @AssertTrue(message = "Informe o nome e um contato (e-mail ou telefone) do responsável.")
        public boolean isResponsavelValido() {
            if (!temResponsavel()) {
                return true;
            }
            var temTelefone = responsavelTelefone != null && responsavelTelefone.chars().anyMatch(Character::isDigit);
            return preenchido(responsavelNome) && (preenchido(responsavelEmail) || temTelefone);
        }

        private static boolean preenchido(String texto) {
            return texto != null && !texto.isBlank();
        }
    }

}
