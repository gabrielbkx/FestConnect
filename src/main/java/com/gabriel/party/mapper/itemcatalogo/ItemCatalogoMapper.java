package com.gabriel.party.mapper.itemcatalogo;

import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoRequestDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemCatalogoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "prestador", ignore = true)
    @Mapping(target = "midias", ignore = true)
    ItemCatalogo toEntity(ItemCatalogoRequestDTO dto);

    ItemCatalogoResponseDTO toDto(ItemCatalogo entidade);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "prestador", ignore = true)
    @Mapping(target = "midias", ignore = true)
    void atualizarItemDoDTO(ItemCatalogoRequestDTO dto, @MappingTarget ItemCatalogo itemCatalogo);
}
