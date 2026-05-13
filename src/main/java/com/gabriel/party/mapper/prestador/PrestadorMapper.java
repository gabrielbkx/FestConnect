package com.gabriel.party.mapper.prestador;

import com.gabriel.party.dtos.prestador.PrestadorRequestDTO;
import com.gabriel.party.dtos.prestador.PrestadorResponseDTO;
import com.gabriel.party.dtos.prestador.PrestadorResumoDTO;
import com.gabriel.party.model.avaliacao.Avaliacao;
import com.gabriel.party.model.prestador.Prestador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PrestadorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    Prestador toEntity(PrestadorRequestDTO dto);

    @Mapping(target = "nome", source = "nomeCompleto")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNome", source = "categoria.nome")
    @Mapping(target = "mediaAvaliacoes", expression = "java(calcularMedia(prestador.getAvaliacoes()))")
    @Mapping(target = "quantidadeAvaliacoes", expression = "java(calcularQuantidade(prestador.getAvaliacoes()))")
    PrestadorResponseDTO toDto(Prestador prestador);

    @Mapping(target = "nome", source = "nomeCompleto")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "categoriaNome", source = "categoria.nome")
    @Mapping(target = "cidade", source = "endereco.cidade")
    @Mapping(target = "estado", source = "endereco.estado")
    @Mapping(target = "mediaAvaliacoes", expression = "java(calcularMedia(prestador.getAvaliacoes()))")
    @Mapping(target = "quantidadeAvaliacoes", expression = "java(calcularQuantidade(prestador.getAvaliacoes()))")
    PrestadorResumoDTO toSummaryDto(Prestador prestador);

    List<PrestadorResumoDTO> toSummaryList(List<Prestador> prestadores);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "categoria", ignore = true)
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
}

