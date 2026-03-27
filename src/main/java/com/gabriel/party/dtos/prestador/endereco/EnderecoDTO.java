package com.gabriel.party.dtos.prestador.endereco;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoDTO(

        @NotBlank String logradouro,
        @NotBlank String bairro,
        @NotBlank @Pattern(regexp = "\\d{8}") String cep,
        @NotBlank String cidade,
        @NotBlank @Size(min = 2, max = 2) String estado,
        String complemento,
        @NotNull
        Integer numero,
        Double latitude,
        Double longitude)  {
}
