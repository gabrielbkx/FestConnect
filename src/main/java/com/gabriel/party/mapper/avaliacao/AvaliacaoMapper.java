package com.gabriel.party.mapper.avaliacao;

import com.gabriel.party.dtos.avaliacao.AvaliacaoRequestDTO;
import com.gabriel.party.dtos.avaliacao.AvaliacaoResponseDTO;
import com.gabriel.party.model.avaliacao.Avaliacao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AvaliacaoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "prestador", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    Avaliacao toEntity(AvaliacaoRequestDTO dto);

    @Mapping(target = "prestadorId", source = "prestador.id")
    AvaliacaoResponseDTO toDto(Avaliacao avaliacao);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "prestador", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    void atualizarAvaliacaoDoDTO(AvaliacaoRequestDTO dto, @MappingTarget Avaliacao avaliacao);
}

