package com.gabriel.party.mapper.itemcatalogo;

import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoRequestDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResumoDTO;
import com.gabriel.party.dtos.itemcatalogo.LocalDetalheDTO;
import com.gabriel.party.mapper.midia.MidiaMapper;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.itemcatalogo.Local;
import com.gabriel.party.model.itemcatalogo.Produto;
import com.gabriel.party.model.itemcatalogo.Servico;
import com.gabriel.party.model.midia.Midia;
import com.gabriel.party.model.midia.enums.TipoMidia;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.SubclassMapping;

import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring",
        uses = { MidiaMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemCatalogoMapper {

    @Named("listaAtivosItens")
    default List<ItemCatalogoResponseDTO> toDtoListAtivos(Collection<ItemCatalogo> itens) {
        if (itens == null || itens.isEmpty()) return Collections.emptyList();
        return itens.stream()
                .filter(i -> Boolean.TRUE.equals(i.getAtivo()))
                .map(this::toDto)
                .toList();
    }

    @SubclassMapping(source = Local.class, target = ItemCatalogoResponseDTO.class)
    @SubclassMapping(source = Servico.class, target = ItemCatalogoResponseDTO.class)
    @SubclassMapping(source = Produto.class, target = ItemCatalogoResponseDTO.class)
    ItemCatalogoResponseDTO toDto(ItemCatalogo item);

    @Mapping(target = "tipo", constant = "local")
    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNome", source = "categoria.nome")
    @Mapping(target = "localDetalhe", expression = "java(montarDetalhe(local))")
    @Mapping(target = "prestadorId", source = "prestador.id")
    @Mapping(target = "prestadorNome", source = "prestador.nomeCompleto")
    @Mapping(target = "prestadorFotoUrl", source = "prestador.fotoPerfilUrl")
    @Mapping(target = "prestadorCidade", source = "prestador.endereco.cidade")
    @Mapping(target = "prestadorEstado", source = "prestador.endereco.estado")
    ItemCatalogoResponseDTO toDto(Local local);

    @Mapping(target = "tipo", constant = "servico")
    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNome", source = "categoria.nome")
    @Mapping(target = "localDetalhe", ignore = true)
    @Mapping(target = "prestadorId", source = "prestador.id")
    @Mapping(target = "prestadorNome", source = "prestador.nomeCompleto")
    @Mapping(target = "prestadorFotoUrl", source = "prestador.fotoPerfilUrl")
    @Mapping(target = "prestadorCidade", source = "prestador.endereco.cidade")
    @Mapping(target = "prestadorEstado", source = "prestador.endereco.estado")
    ItemCatalogoResponseDTO toDto(Servico servico);

    @Mapping(target = "tipo", constant = "produto")
    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNome", source = "categoria.nome")
    @Mapping(target = "localDetalhe", ignore = true)
    @Mapping(target = "prestadorId", source = "prestador.id")
    @Mapping(target = "prestadorNome", source = "prestador.nomeCompleto")
    @Mapping(target = "prestadorFotoUrl", source = "prestador.fotoPerfilUrl")
    @Mapping(target = "prestadorCidade", source = "prestador.endereco.cidade")
    @Mapping(target = "prestadorEstado", source = "prestador.endereco.estado")
    ItemCatalogoResponseDTO toDto(Produto produto);

    default LocalDetalheDTO montarDetalhe(Local l) {
        if (l == null) return null;
        return new LocalDetalheDTO(
                l.getCapacidadeMaxima(),
                l.getMetragem(),
                l.getPermiteSom(),
                l.getTemEstacionamento(),
                l.getTipoEspaco()
        );
    }

    @ObjectFactory
    default ItemCatalogo criarItemConcreto(ItemCatalogoRequestDTO dto) {
        return switch (dto.tipo()) {
            case LOCAL -> new Local();
            case SERVICO -> new Servico();
            case PRODUTO -> new Produto();
        };
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "prestador", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "midias", ignore = true)
    ItemCatalogo toEntity(ItemCatalogoRequestDTO dto);

    @AfterMapping
    default void preencherDetalheLocal(ItemCatalogoRequestDTO dto, @MappingTarget ItemCatalogo item) {
        if (item instanceof Local local && dto.localDetalhe() != null) {
            local.setCapacidadeMaxima(dto.localDetalhe().capacidadeMaxima());
            local.setMetragem(dto.localDetalhe().metragem());
            local.setPermiteSom(dto.localDetalhe().permiteSom());
            local.setTemEstacionamento(dto.localDetalhe().temEstacionamento());
            local.setTipoEspaco(dto.localDetalhe().tipoEspaco());
        }
    }

    @Mapping(target = "tipo", expression = "java(tipoDoItem(item))")
    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNome", source = "categoria.nome")
    @Mapping(target = "prestadorId", source = "prestador.id")
    @Mapping(target = "prestadorNome", source = "prestador.nomeCompleto")
    @Mapping(target = "cidade", source = "prestador.endereco.cidade")
    @Mapping(target = "estado", source = "prestador.endereco.estado")
    @Mapping(target = "fotoPrincipalUrl", expression = "java(primeiraFotoUrl(item))")
    ItemCatalogoResumoDTO toResumoDto(ItemCatalogo item);

    default String tipoDoItem(ItemCatalogo item) {
        if (item instanceof Local) return "local";
        if (item instanceof Servico) return "servico";
        if (item instanceof Produto) return "produto";
        return null;
    }

    default String primeiraFotoUrl(ItemCatalogo item) {
        if (item == null || item.getMidias() == null) return null;
        return item.getMidias().stream()
                .filter(m -> m.getTipo() == TipoMidia.FOTO)
                .min(Comparator.comparing(Midia::getOrdem, Comparator.nullsLast(Integer::compare)))
                .map(Midia::getUrl)
                .orElse(null);
    }

    default void atualizarItemDoDTO(ItemCatalogoRequestDTO dto, @MappingTarget ItemCatalogo item) {
        if (dto.titulo() != null) item.setTitulo(dto.titulo());
        if (dto.descricao() != null) item.setDescricao(dto.descricao());
        if (dto.precoBase() != null) item.setPrecoBase(dto.precoBase());

        if (item instanceof Local local && dto.localDetalhe() != null) {
            LocalDetalheDTO d = dto.localDetalhe();
            if (d.capacidadeMaxima() != null) local.setCapacidadeMaxima(d.capacidadeMaxima());
            if (d.metragem() != null) local.setMetragem(d.metragem());
            if (d.permiteSom() != null) local.setPermiteSom(d.permiteSom());
            if (d.temEstacionamento() != null) local.setTemEstacionamento(d.temEstacionamento());
            if (d.tipoEspaco() != null) local.setTipoEspaco(d.tipoEspaco());
        }
    }
}
