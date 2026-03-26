package com.gabriel.party.model.prestador.endereco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(

        @NotBlank String logradouro,
        @NotBlank String bairro,
        @NotBlank @Pattern(regexp = "\\d{8}") String cep,
        @NotBlank String cidade,
        @NotBlank @Size(min = 2, max = 2) String Estado,
        String complemento,
        @NotNull
        Integer numero)  {
}
