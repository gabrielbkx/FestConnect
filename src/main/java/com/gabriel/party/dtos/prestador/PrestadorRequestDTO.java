package com.gabriel.party.dtos.prestador;

import com.gabriel.party.dtos.prestador.endereco.EnderecoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Schema(description = "Dados para criar ou atualizar o perfil de um prestador")
public record PrestadorRequestDTO(

        @Schema(description = "Nome completo ou nome fantasia do prestador", example = "Buffet Silva & Cia")
        @NotBlank String nome,

        @Schema(description = "E-mail de contato do prestador", example = "contato@buffetsilva.com.br")
        @NotBlank @Email(message = " ")
        String email,

        @Schema(description = "Descrição dos serviços oferecidos", example = "Buffet especializado em eventos corporativos e casamentos, com 10 anos de experiência.")
        String descricao,

        @Schema(description = "Número de WhatsApp para contato (somente dígitos, DDD + número)", example = "11987654321")
        @NotNull(message = "O número de WhatsApp é obrigatório")
        @Pattern(regexp = "\\d{10,11}", message = "O número de WhatsApp deve conter apenas dígitos e ter entre 10 e 11 caracteres")
        String whatsapp,

        @Schema(description = "Endereço do prestador. As coordenadas (lat/lon) são preenchidas automaticamente via geocoding.")
        EnderecoDTO endereco,

        @Schema(description = "ID da categoria principal do prestador (opcional). Define a identidade visual; cada item do catálogo tem sua própria categoria.",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID categoriaPrincipalId
) {}
