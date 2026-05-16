package com.gabriel.party.mapper.prestador;

import com.gabriel.party.dtos.avaliacao.AvaliacaoResponseDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.dtos.prestador.PrestadorRequestDTO;
import com.gabriel.party.dtos.prestador.PrestadorResponseDTO;
import com.gabriel.party.dtos.prestador.PrestadorResumoDTO;
import com.gabriel.party.mapper.avaliacao.AvaliacaoMapper;
import com.gabriel.party.mapper.itemcatalogo.ItemCatalogoMapper;
import com.gabriel.party.model.avaliacao.Avaliacao;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.prestador.Prestador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PrestadorMapper {

    @Autowired
    protected ItemCatalogoMapper itemCatalogoMapper;

    @Autowired
    protected AvaliacaoMapper avaliacaoMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "categoriaPrincipal", ignore = true)
    public abstract Prestador toEntity(PrestadorRequestDTO dto);

    @Mapping(target = "nome", source = "nomeCompleto")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "categoriaPrincipalId", source = "categoriaPrincipal.id")
    @Mapping(target = "categoriaPrincipalNome", source = "categoriaPrincipal.nome")
    @Mapping(target = "categorias", expression = "java(extrairCategorias(prestador.getItensCatalogo()))")
    @Mapping(target = "itensCatalogo", expression = "java(mapearItensAtivos(prestador.getItensCatalogo()))")
    @Mapping(target = "avaliacoes", expression = "java(mapearAvaliacoesAtivas(prestador.getAvaliacoes()))")
    @Mapping(target = "mediaAvaliacoes", expression = "java(calcularMedia(prestador.getAvaliacoes()))")
    @Mapping(target = "quantidadeAvaliacoes", expression = "java(calcularQuantidade(prestador.getAvaliacoes()))")
    public abstract PrestadorResponseDTO toDto(Prestador prestador);

    @Mapping(target = "nome", source = "nomeCompleto")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "categoriaPrincipalNome", source = "categoriaPrincipal.nome")
    @Mapping(target = "categorias", expression = "java(extrairNomesCategorias(prestador.getItensCatalogo()))")
    @Mapping(target = "cidade", source = "endereco.cidade")
    @Mapping(target = "estado", source = "endereco.estado")
    @Mapping(target = "mediaAvaliacoes", expression = "java(calcularMedia(prestador.getAvaliacoes()))")
    @Mapping(target = "quantidadeAvaliacoes", expression = "java(calcularQuantidade(prestador.getAvaliacoes()))")
    public abstract PrestadorResumoDTO toSummaryDto(Prestador prestador);

    public abstract List<PrestadorResumoDTO> toSummaryList(List<Prestador> prestadores);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "categoriaPrincipal", ignore = true)
    public abstract void atualizarPrestadorDoDTO(PrestadorRequestDTO dto, @MappingTarget Prestador prestador);

    protected Double calcularMedia(Collection<Avaliacao> avaliacoes) {
        if (avaliacoes == null || avaliacoes.isEmpty()) return null;
        return avaliacoes.stream()
                .filter(a -> Boolean.TRUE.equals(a.getAtivo()))
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);
    }

    protected Integer calcularQuantidade(Collection<Avaliacao> avaliacoes) {
        if (avaliacoes == null) return 0;
        return (int) avaliacoes.stream().filter(a -> Boolean.TRUE.equals(a.getAtivo())).count();
    }

    protected List<ItemCatalogoResponseDTO> mapearItensAtivos(Collection<ItemCatalogo> itens) {
        if (itens == null || itens.isEmpty()) return Collections.emptyList();
        return itens.stream()
                .filter(i -> Boolean.TRUE.equals(i.getAtivo()))
                .map(itemCatalogoMapper::toDto)
                .toList();
    }

    protected List<AvaliacaoResponseDTO> mapearAvaliacoesAtivas(Collection<Avaliacao> avaliacoes) {
        if (avaliacoes == null || avaliacoes.isEmpty()) return Collections.emptyList();
        return avaliacoes.stream()
                .filter(a -> Boolean.TRUE.equals(a.getAtivo()))
                .map(avaliacaoMapper::toDto)
                .toList();
    }

    protected List<PrestadorResponseDTO.CategoriaResumoDTO> extrairCategorias(Collection<ItemCatalogo> itens) {
        if (itens == null || itens.isEmpty()) return Collections.emptyList();
        Map<java.util.UUID, PrestadorResponseDTO.CategoriaResumoDTO> map = new LinkedHashMap<>();
        for (ItemCatalogo item : itens) {
            if (Boolean.FALSE.equals(item.getAtivo())) continue;
            if (item.getCategoria() == null) continue;
            map.putIfAbsent(item.getCategoria().getId(),
                    new PrestadorResponseDTO.CategoriaResumoDTO(
                            item.getCategoria().getId(),
                            item.getCategoria().getNome()));
        }
        return List.copyOf(map.values());
    }

    protected List<String> extrairNomesCategorias(Collection<ItemCatalogo> itens) {
        if (itens == null || itens.isEmpty()) return Collections.emptyList();
        return itens.stream()
                .filter(i -> Boolean.TRUE.equals(i.getAtivo()))
                .filter(i -> i.getCategoria() != null)
                .map(i -> i.getCategoria().getNome())
                .distinct()
                .toList();
    }
}