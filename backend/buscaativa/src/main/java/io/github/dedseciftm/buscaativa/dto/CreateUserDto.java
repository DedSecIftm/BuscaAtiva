package io.github.dedseciftm.buscaativa.dto;

import io.github.dedseciftm.buscaativa.model.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserDto(

        @NotBlank(message = "Informe o nome.")
        @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
        String nome,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "Informe um e-mail válido.")
        String email,

        @NotBlank(message = "Informe a senha.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String password,

        @NotNull(message = "Informe o papel do usuário.")
        RoleName role

) {
}
