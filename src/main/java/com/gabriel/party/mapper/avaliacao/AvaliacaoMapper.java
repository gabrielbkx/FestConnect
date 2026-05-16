package com.gabriel.party.mapper.avaliacao;

import com.gabriel.party.dtos.avaliacao.AvaliacaoCreateDTO;
import com.gabriel.party.dtos.avaliacao.AvaliacaoRequestDTO;
import com.gabriel.party.dtos.avaliacao.AvaliacaoResponseDTO;
import com.gabriel.party.model.avaliacao.Avaliacao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AvaliacaoMapper {

    @Named("listaAtivasAvaliacoes")
    default List<AvaliacaoResponseDTO> toDtoListAtivas(Collection<Avaliacao> avaliacoes) {
        if (avaliacoes == null || avaliacoes.isEmpty()) return Collections.emptyList();
        return avaliacoes.stream()
                .filter(a -> Boolean.TRUE.equals(a.getAtivo()))
                .map(this::toDto)
                .toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "prestador", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "pedido", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    Avaliacao toEntity(AvaliacaoCreateDTO dto);

    @Mapping(target = "prestadorId", source = "prestador.id")
    @Mapping(target = "prestadorNome", source = "prestador.nomeCompleto")
    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "clienteNome", source = "cliente.nomeCompleto")
    @Mapping(target = "pedidoId", source = "pedido.id")
    @Mapping(target = "itemCatalogoId", source = "pedido.itemCatalogo.id")
    @Mapping(target = "itemCatalogoTitulo", source = "pedido.itemCatalogo.titulo")
    AvaliacaoResponseDTO toDto(Avaliacao avaliacao);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "prestador", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "pedido", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    void atualizarAvaliacaoDoDTO(AvaliacaoRequestDTO dto, @MappingTarget Avaliacao avaliacao);
}
