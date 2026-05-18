package com.gabriel.party.mapper.prestador;

import com.gabriel.party.dtos.prestador
        .PrestadorRequestDTO;
import com.gabriel.party.dtos.prestador.PrestadorAdminDTO;
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring",
        uses = { ItemCatalogoMapper.class, AvaliacaoMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PrestadorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    Prestador toEntity(PrestadorRequestDTO dto);

    @Mapping(target = "nome", source = "nomeCompleto")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "categorias", expression = "java(extrairCategorias(prestador.getItensCatalogo()))")
    @Mapping(target = "itensCatalogo", source = "itensCatalogo", qualifiedByName = "listaAtivosItens")
    @Mapping(target = "avaliacoes", source = "avaliacoes", qualifiedByName = "listaAtivasAvaliacoes")
    @Mapping(target = "mediaAvaliacoes", expression = "java(calcularMedia(prestador.getAvaliacoes()))")
    @Mapping(target = "quantidadeAvaliacoes", expression = "java(calcularQuantidade(prestador.getAvaliacoes()))")
    PrestadorResponseDTO toDto(Prestador prestador);

    @Mapping(target = "nome", source = "nomeCompleto")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "categorias", expression = "java(extrairNomesCategorias(prestador.getItensCatalogo()))")
    @Mapping(target = "cidade", source = "endereco.cidade")
    @Mapping(target = "estado", source = "endereco.estado")
    @Mapping(target = "mediaAvaliacoes", expression = "java(calcularMedia(prestador.getAvaliacoes()))")
    @Mapping(target = "quantidadeAvaliacoes", expression = "java(calcularQuantidade(prestador.getAvaliacoes()))")
    PrestadorResumoDTO toSummaryDto(Prestador prestador);

    List<PrestadorResumoDTO> toSummaryList(List<Prestador> prestadores);

    // Mapeamento administrativo — inclui PII (cnpjOuCpf, endereco completo).
    // Usado SOMENTE em endpoints /prestadores/{id}/admin restritos a ROLE_ADMINISTRADOR.
    @Mapping(target = "nome", source = "nomeCompleto")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "dataCriacao", source = "usuario.dataCriacao")
    @Mapping(target = "categorias", expression = "java(extrairNomesCategorias(prestador.getItensCatalogo()))")
    @Mapping(target = "totalItens", expression = "java(contarItensAtivos(prestador.getItensCatalogo()))")
    @Mapping(target = "mediaAvaliacoes", expression = "java(calcularMedia(prestador.getAvaliacoes()))")
    @Mapping(target = "quantidadeAvaliacoes", expression = "java(calcularQuantidade(prestador.getAvaliacoes()))")
    PrestadorAdminDTO toAdminDto(Prestador prestador);

    default Integer contarItensAtivos(Collection<ItemCatalogo> itens) {
        if (itens == null) return 0;
        return (int) itens.stream().filter(i -> Boolean.TRUE.equals(i.getAtivo())).count();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    void atualizarPrestadorDoDTO(PrestadorRequestDTO dto, @MappingTarget Prestador prestador);

    default Double calcularMedia(Collection<Avaliacao> avaliacoes) {
        if (avaliacoes == null || avaliacoes.isEmpty()) return null;
        return avaliacoes.stream()
                .filter(a -> Boolean.TRUE.equals(a.getAtivo()))
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);
    }

    default Integer calcularQuantidade(Collection<Avaliacao> avaliacoes) {
        if (avaliacoes == null) return 0;
        return (int) avaliacoes.stream().filter(a -> Boolean.TRUE.equals(a.getAtivo())).count();
    }

    default List<PrestadorResponseDTO.CategoriaResumoDTO> extrairCategorias(Collection<ItemCatalogo> itens) {
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

    default List<String> extrairNomesCategorias(Collection<ItemCatalogo> itens) {
        if (itens == null || itens.isEmpty()) return Collections.emptyList();
        return itens.stream()
                .filter(i -> Boolean.TRUE.equals(i.getAtivo()))
                .filter(i -> i.getCategoria() != null)
                .map(i -> i.getCategoria().getNome())
                .distinct()
                .toList();
    }
}
