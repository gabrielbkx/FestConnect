package com.gabriel.party.mapper.itemcatalogo;

import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoRequestDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.dtos.itemcatalogo.LocalDetalheDTO;
import com.gabriel.party.mapper.midia.MidiaMapper;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.itemcatalogo.Local;
import com.gabriel.party.model.itemcatalogo.Produto;
import com.gabriel.party.model.itemcatalogo.Servico;
import com.gabriel.party.model.itemcatalogo.enums.TipoItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ItemCatalogoMapper {

    private final MidiaMapper midiaMapper;

    public ItemCatalogoMapper(MidiaMapper midiaMapper) {
        this.midiaMapper = midiaMapper;
    }

    public ItemCatalogo toEntity(ItemCatalogoRequestDTO dto) {
        ItemCatalogo item = switch (dto.tipo()) {
            case LOCAL -> {
                Local local = new Local();
                if (dto.localDetalhe() != null) {
                    local.setCapacidadeMaxima(dto.localDetalhe().capacidadeMaxima());
                    local.setMetragem(dto.localDetalhe().metragem());
                    local.setPermiteSom(dto.localDetalhe().permiteSom());
                    local.setTemEstacionamento(dto.localDetalhe().temEstacionamento());
                    local.setTipoEspaco(dto.localDetalhe().tipoEspaco());
                }
                yield local;
            }
            case SERVICO -> new Servico();
            default -> new Produto();
        };
        item.setTitulo(dto.titulo());
        item.setDescricao(dto.descricao());
        item.setPrecoBase(dto.precoBase());
        item.setAtivo(true);
        return item;
    }

    public ItemCatalogoResponseDTO toDto(ItemCatalogo item) {
        List<com.gabriel.party.dtos.midia.MidiaResponseDTO> midias =
                item.getMidias() != null
                        ? item.getMidias().stream().map(midiaMapper::toDto).toList()
                        : Collections.emptyList();

        java.util.UUID categoriaId = item.getCategoria() != null ? item.getCategoria().getId() : null;
        String categoriaNome = item.getCategoria() != null ? item.getCategoria().getNome() : null;

        if (item instanceof Local local) {
            LocalDetalheDTO detalhe = new LocalDetalheDTO(
                    local.getCapacidadeMaxima(),
                    local.getMetragem(),
                    local.getPermiteSom(),
                    local.getTemEstacionamento(),
                    local.getTipoEspaco()
            );
            return new ItemCatalogoResponseDTO(
                    local.getId(), local.getTitulo(), local.getDescricao(),
                    local.getPrecoBase(), TipoItem.LOCAL.getValor(),
                    categoriaId, categoriaNome,
                    local.getAtivo(), midias, detalhe
            );
        }

        String tipo = item instanceof Servico ? TipoItem.SERVICO.getValor() : TipoItem.PRODUTO.getValor();
        return new ItemCatalogoResponseDTO(
                item.getId(), item.getTitulo(), item.getDescricao(),
                item.getPrecoBase(), tipo, categoriaId, categoriaNome,
                item.getAtivo(), midias, null
        );
    }

    public void atualizarItemDoDTO(ItemCatalogoRequestDTO dto, ItemCatalogo item) {
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
