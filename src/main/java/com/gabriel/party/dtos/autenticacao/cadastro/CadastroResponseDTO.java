package com.gabriel.party.dtos.autenticacao.cadastro;

import com.gabriel.party.dtos.autenticacao.login.TokenResponseDTO;

import java.util.UUID;

public record CadastroResponseDTO(
        UUID id,
        String nomeCompleto,
        String email,
        String token
        ) {
}
