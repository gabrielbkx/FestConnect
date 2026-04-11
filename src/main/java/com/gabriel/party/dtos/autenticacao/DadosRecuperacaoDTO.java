package com.gabriel.party.dtos.autenticacao;

import jakarta.validation.constraints.Email;

public record DadosRecuperacaoDTO(
        @Email(message = "O email deve ser válido")
        String email) {
}
