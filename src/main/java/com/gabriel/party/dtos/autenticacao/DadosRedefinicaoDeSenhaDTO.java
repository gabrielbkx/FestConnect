package com.gabriel.party.dtos.autenticacao;

import jakarta.validation.constraints.NotNull;

public record DadosRedefinicaoDeSenhaDTO(
        @NotNull(message = "O token de recuperação de senha é obrigatório")
        String token,
        @NotNull(message = "A nova senha é obrigatória")
        String novaSenha) {
}
