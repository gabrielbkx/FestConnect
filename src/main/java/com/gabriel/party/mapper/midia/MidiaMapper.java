package com.gabriel.party.mapper.midia;

import com.gabriel.party.dtos.midia.MidiaRequestDTO;
import com.gabriel.party.dtos.midia.MidiaResponseDTO;
import com.gabriel.party.model.midia.Midia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MidiaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "prestador", ignore = true)
    Midia toEntity(MidiaRequestDTO dto);

    @Mapping(target = "prestadorId", source = "prestador.id")
    MidiaResponseDTO toDto(Midia midia);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "prestador", ignore = true)
    void atualizarMidiaDoDTO(MidiaRequestDTO dto, @MappingTarget Midia midia);
}

