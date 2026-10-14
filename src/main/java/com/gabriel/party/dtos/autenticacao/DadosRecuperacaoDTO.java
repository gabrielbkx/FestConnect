package com.gabriel.party.dtos.autenticacao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DadosRecuperacaoDTO(
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        String email) {
}
