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

    // Respostas de categoria única (criar/atualizar/buscar): a contagem não é o foco, fica 0.
    @Mapping(target = "quantidadePrestadores", constant = "0")
    CategoriaReponseDTO toDto(Categoria categoria);

    // Listagem: recebe a contagem real de prestadores vinda da consulta agrupada.
    @Mapping(target = "quantidadePrestadores", source = "quantidadePrestadores")
    CategoriaReponseDTO toDto(Categoria categoria, Integer quantidadePrestadores);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    void atualizarCategoriaDoDTO(CategoriaRequestDTO dto, @MappingTarget Categoria categoria);
}
