package com.gabriel.party.dtos.prestador;

import com.gabriel.party.dtos.avaliacao.AvaliacaoResponseDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.dtos.midia.MidiaResponseDTO;
import com.gabriel.party.dtos.prestador.endereco.EnderecoDTO;
import com.gabriel.party.model.avaliacao.Avaliacao;
import com.gabriel.party.model.midia.Midia;

import java.util.List;
import java.util.UUID;

public record PrestadorResponseDTO(
        UUID id,
        String nome,
        String email,
        String whatsapp,
        UUID categoriaId,
        String categoriaNome,

        EnderecoDTO endereco,

        List<ItemCatalogoResponseDTO> itensCatalogo,
        List<MidiaResponseDTO> midias,
        List<AvaliacaoResponseDTO> avaliacoes,

        Double mediaAvaliacoes,
        Integer quantidadeAvaliacoes

) {}

