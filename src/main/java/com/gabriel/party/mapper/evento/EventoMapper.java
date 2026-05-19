package com.gabriel.party.mapper.evento;

import com.gabriel.party.dtos.evento.EventoRequestDTO;
import com.gabriel.party.dtos.evento.EventoResponseDTO;
import com.gabriel.party.dtos.evento.PrestadorSugestaoDTO;
import com.gabriel.party.model.avaliacao.Avaliacao;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.evento.Evento;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.prestador.Prestador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PLANEJANDO")
    @Mapping(target = "cliente", source = "cliente")
    @Mapping(target = "pedidos", ignore = true)
    @Mapping(target = "dataHoraCriacao", ignore = true)
    @Mapping(target = "dataHoraAtualizacao", ignore = true)
    @Mapping(target = "nome", source = "dto.nome")
    @Mapping(target = "tipoEvento", source = "dto.tipoEvento")
    @Mapping(target = "dataEvento", source = "dto.dataEvento")
    @Mapping(target = "cidade", source = "dto.cidade")
    @Mapping(target = "bairro", source = "dto.bairro")
    @Mapping(target = "endereco", source = "dto.endereco")
    @Mapping(target = "numeroConvidados", source = "dto.numeroConvidados")
    @Mapping(target = "observacoes", source = "dto.observacoes")
    Evento toEntity(EventoRequestDTO dto, Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "pedidos", ignore = true)
    @Mapping(target = "dataHoraCriacao", ignore = true)
    @Mapping(target = "dataHoraAtualizacao", ignore = true)
    @Mapping(target = "nome", source = "nome")
    @Mapping(target = "tipoEvento", source = "tipoEvento")
    @Mapping(target = "dataEvento", source = "dataEvento")
    @Mapping(target = "cidade", source = "cidade")
    @Mapping(target = "bairro", source = "bairro")
    @Mapping(target = "endereco", source = "endereco")
    @Mapping(target = "numeroConvidados", source = "numeroConvidados")
    @Mapping(target = "observacoes", source = "observacoes")
    void atualizarEventoDoDTO(EventoRequestDTO dto, @MappingTarget Evento evento);

    @Mapping(target = "quantidadePedidos", expression = "java(contarPedidos(evento.getPedidos()))")
    EventoResponseDTO toResponseDTO(Evento evento);

    List<EventoResponseDTO> toResponseList(List<Evento> eventos);

    @Mapping(target = "cidade", source = "endereco.cidade")
    @Mapping(target = "bairro", source = "endereco.bairro")
    @Mapping(target = "mediaAvaliacoes", expression = "java(calcularMedia(prestador.getAvaliacoes()))")
    @Mapping(target = "quantidadeAvaliacoes", expression = "java(calcularQuantidade(prestador.getAvaliacoes()))")
    PrestadorSugestaoDTO toSugestaoDTO(Prestador prestador);

    List<PrestadorSugestaoDTO> toSugestaoList(List<Prestador> prestadores);

    default Integer contarPedidos(Collection<Pedido> pedidos) {
        return pedidos == null ? 0 : pedidos.size();
    }

    default BigDecimal calcularMedia(Collection<Avaliacao> avaliacoes) {
        if (avaliacoes == null || avaliacoes.isEmpty()) return BigDecimal.ZERO;
        double media = avaliacoes.stream()
                .filter(a -> Boolean.TRUE.equals(a.getAtivo()))
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);
        return BigDecimal.valueOf(media).setScale(2, RoundingMode.HALF_UP);
    }

    default Integer calcularQuantidade(Collection<Avaliacao> avaliacoes) {
        if (avaliacoes == null) return 0;
        return (int) avaliacoes.stream().filter(a -> Boolean.TRUE.equals(a.getAtivo())).count();
    }
}
