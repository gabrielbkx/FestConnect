package com.gabriel.party.dtos.prestador;

import com.gabriel.party.dtos.prestador.endereco.EnderecoDTO;

import java.util.UUID;

public record PrestadorResponseDTO(
        UUID id,
        String nome,
        String email,
        String whatsapp,
        UUID categoriaId,
        String categoriaNome,

        EnderecoDTO endereco
) {}

