package com.gabriel.party.dtos.prestador.endereco;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Endereço completo")
public record EnderecoDTO(

        @Schema(description = "Nome da rua ou avenida", example = "Avenida Paulista")
        @NotBlank String logradouro,

        @Schema(description = "Bairro", example = "Bela Vista")
        @NotBlank String bairro,

        @Schema(description = "CEP somente com dígitos, sem hífen", example = "01310100")
        @NotBlank @Pattern(regexp = "\\d{8}") String cep,

        @Schema(description = "Cidade", example = "São Paulo")
        @NotBlank String cidade,

        @Schema(description = "Sigla do estado (2 letras maiúsculas)", example = "SP")
        @NotBlank @Size(min = 2, max = 2) String estado,

        @Schema(description = "Complemento (apto, sala, etc.)", example = "Apto 42")
        String complemento,

        @Schema(description = "Número do imóvel", example = "1578")
        @NotNull Integer numero,

        @Schema(description = "Latitude geográfica. Preenchida automaticamente via geocoding — não enviar.", example = "-23.5505")
        Double latitude,

        @Schema(description = "Longitude geográfica. Preenchida automaticamente via geocoding — não enviar.", example = "-46.6333")
        Double longitude
) {}
