package com.gabriel.party.dtos.cliente;

import com.gabriel.party.dtos.prestador.endereco.EnderecoDTO;
import java.util.UUID;

public record ClienteResponseDTO(
        UUID id,
        String nome,
        String email,
        String whatsapp,
        EnderecoDTO endereco,
        Boolean ativo
) {}

