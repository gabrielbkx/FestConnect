package com.gabriel.party.dtos.autenticacao.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDTO(
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        String email,
        @NotNull(message = "A senha é obrigatória")
        String senha) {
}
