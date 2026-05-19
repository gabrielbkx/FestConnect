package com.gabriel.party.dtos.evento;

import com.gabriel.party.model.evento.enums.TipoEvento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Dados para criar ou atualizar um evento")
public record EventoRequestDTO(

        @Schema(description = "Nome do evento", example = "Casamento Maria e João")
        @NotBlank(message = "O nome do evento é obrigatório")
        String nome,

        @Schema(description = "Tipo do evento", example = "casamento")
        @NotNull(message = "O tipo de evento é obrigatório")
        TipoEvento tipoEvento,

        @Schema(description = "Data e hora do evento. Deve ser futura. Formato: ISO-8601", example = "2026-12-31T18:00:00")
        @NotNull(message = "A data do evento é obrigatória")
        @Future(message = "A data do evento deve ser no futuro")
        LocalDateTime dataEvento,

        @Schema(description = "Cidade do evento", example = "São Paulo")
        @NotBlank(message = "A cidade é obrigatória")
        String cidade,

        @Schema(description = "Bairro do evento", example = "Pinheiros")
        @NotBlank(message = "O bairro é obrigatório")
        String bairro,

        @Schema(description = "Endereço completo do evento", example = "Rua das Flores, 100")
        @NotBlank(message = "O endereço é obrigatório")
        String endereco,

        @Schema(description = "Número estimado de convidados", example = "150")
        @NotNull(message = "O número de convidados é obrigatório")
        @Min(value = 1, message = "O evento deve ter pelo menos 1 convidado")
        Integer numeroConvidados,

        @Schema(description = "Observações livres sobre o evento", example = "Decoração rústica, comida de boteco")
        String observacoes
) {}
