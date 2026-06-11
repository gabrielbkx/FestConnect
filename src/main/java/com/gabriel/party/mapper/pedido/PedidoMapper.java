package com.gabriel.party.mapper.pedido;

import com.gabriel.party.dtos.pedido.OrcamentoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoResponseDTO;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.evento.Evento;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.prestador.Prestador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "valor", ignore = true)
    @Mapping(target = "detalhesOrcamento", ignore = true)
    @Mapping(target = "validadeOrcamento", ignore = true)
    @Mapping(target = "dataHoraCriacao", ignore = true)
    @Mapping(target = "dataHoraAtualizacao", ignore = true)
    @Mapping(target = "prazoResposta", ignore = true)
    @Mapping(target = "statusPedido", constant = "PENDENTE")
    @Mapping(source = "cliente", target = "cliente")
    @Mapping(source = "prestador", target = "prestador")
    @Mapping(source = "itemCatalogo", target = "itemCatalogo")
    @Mapping(source = "evento", target = "evento")
    @Mapping(source = "dto.descricao", target = "descricao")
    @Mapping(source = "dto.dataEvento", target = "dataEvento")
    @Mapping(source = "dto.tipoEvento", target = "tipoEvento")
    @Mapping(source = "dto.numeroConvidados", target = "numeroConvidados")
    @Mapping(source = "dto.localEvento", target = "localEvento")
    Pedido toEntity(PedidoRequestDTO dto, Cliente cliente, Prestador prestador, ItemCatalogo itemCatalogo, Evento evento);

    @Mapping(source = "cliente.nomeCompleto", target = "nomeCliente")
    @Mapping(source = "cliente.fotoPerfilUrl", target = "fotoClienteUrl")
    @Mapping(source = "prestador.id", target = "prestadorId")
    @Mapping(source = "prestador.nomeCompleto", target = "nomePrestador")
    @Mapping(source = "prestador.whatsapp", target = "whatsappPrestador")
    @Mapping(source = "itemCatalogo.id", target = "itemCatalogoId")
    @Mapping(source = "itemCatalogo.titulo", target = "itemCatalogoTitulo")
    @Mapping(source = "statusPedido", target = "status")
    @Mapping(source = "evento.id", target = "eventoId")
    @Mapping(source = "evento.nome", target = "eventoNome")
    @Mapping(target = "fotoPrestadorUrl", source = "prestador.fotoPerfilUrl")
    PedidoResponseDTO toResponseDTO(Pedido pedido);
    List<PedidoResponseDTO> toResponseList(List<Pedido> pedidos);

    void updatePedidoFromOrcamento(OrcamentoRequestDTO dto, @MappingTarget Pedido pedido);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "valor", ignore = true)
    @Mapping(target = "detalhesOrcamento", ignore = true)
    @Mapping(target = "validadeOrcamento", ignore = true)
    @Mapping(target = "dataHoraCriacao", ignore = true)
    @Mapping(target = "dataHoraAtualizacao", ignore = true)
    @Mapping(target = "prazoResposta", ignore = true)
    @Mapping(target = "statusPedido", constant = "PENDENTE")
    @Mapping(source = "cliente", target = "cliente")
    @Mapping(source = "prestador", target = "prestador")
    @Mapping(source = "itemCatalogo", target = "itemCatalogo")
    @Mapping(source = "evento", target = "evento")
    @Mapping(source = "evento.dataEvento", target = "dataEvento")
    @Mapping(source = "evento.numeroConvidados", target = "numeroConvidados")
    @Mapping(target = "localEvento",  expression = "java(evento.getEndereco() + \", \" + evento.getBairro() + \" - \" + evento.getCidade())")
    @Mapping(target = "tipoEvento",   expression = "java(evento.getTipoEvento().name())")
    @Mapping(target = "descricao",    expression = "java(evento.getObservacoes() != null ? evento.getObservacoes() : \"\")")
    Pedido toEntityFromEvento(Cliente cliente, Prestador prestador, ItemCatalogo itemCatalogo, Evento evento);
}
