package com.gabriel.party.dtos.evento;

import com.gabriel.party.model.evento.enums.StatusEvento;
import com.gabriel.party.model.evento.enums.TipoEvento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados resumidos de um evento")
public record EventoResponseDTO(

        @Schema(description = "ID único do evento")
        UUID id,

        @Schema(description = "Nome do evento")
        String nome,

        @Schema(description = "Tipo do evento")
        TipoEvento tipoEvento,

        @Schema(description = "Data e hora do evento")
        LocalDateTime dataEvento,

        @Schema(description = "Cidade do evento")
        String cidade,

        @Schema(description = "Bairro do evento")
        String bairro,

        @Schema(description = "Endereço completo do evento")
        String endereco,

        @Schema(description = "Número de convidados")
        Integer numeroConvidados,

        @Schema(description = "Observações livres")
        String observacoes,

        @Schema(description = "Status atual",
                allowableValues = {"planejando", "confirmado", "cancelado"})
        StatusEvento status,

        @Schema(description = "Quantidade de pedidos vinculados ao evento")
        Integer quantidadePedidos,

        @Schema(description = "Data e hora de criação")
        LocalDateTime dataHoraCriacao
) {}
