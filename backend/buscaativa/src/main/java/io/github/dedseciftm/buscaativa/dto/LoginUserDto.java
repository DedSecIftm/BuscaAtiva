package io.github.dedseciftm.buscaativa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginUserDto(

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "Informe um e-mail válido.")
        String email,

        @NotBlank(message = "Informe a senha.")
        String password

) {

        // Remove espaços do e-mail antes da validação (a normalização final é feita no UserService)
        public LoginUserDto {
                email = email == null ? null : email.trim();
        }
}