package com.gabriel.party.mapper.categoria;

import com.gabriel.party.dtos.categoria.CategoriaReponseDTO;
import com.gabriel.party.dtos.categoria.CategoriaRequestDTO;
import com.gabriel.party.model.categoria.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CategoriaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    Categoria toEntity(CategoriaRequestDTO dto);

    @Mapping(target = "quantidadePrestadores", constant = "0")
    CategoriaReponseDTO toDto(Categoria categoria);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    void atualizarCategoriaDoDTO(CategoriaRequestDTO dto, @MappingTarget Categoria categoria);
}
