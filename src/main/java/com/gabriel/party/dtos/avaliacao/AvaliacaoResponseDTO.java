package com.gabriel.party.dtos.avaliacao;

import java.time.LocalDateTime;
import java.util.UUID;

public record AvaliacaoResponseDTO(
        UUID id,
        Integer nota,
        String comentario,
        LocalDateTime dataCriacao,
        UUID prestadorId,
        String prestadorNome,
        UUID clineteId,
        String clienteNome

) {}

