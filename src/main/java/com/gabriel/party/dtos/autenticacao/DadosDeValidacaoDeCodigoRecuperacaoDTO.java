package com.gabriel.party.dtos.autenticacao;

import jakarta.validation.constraints.Pattern;

public record DadosDeValidacaoDeCodigoRecuperacaoDTO(
        String email,
        @Pattern(regexp = "^\\d{6}$", message = "O código PIN deve conter exatamente 6 dígitos numéricos.")
        String codigoPin) {
}
