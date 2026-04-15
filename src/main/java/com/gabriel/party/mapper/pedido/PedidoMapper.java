package com.gabriel.party.mapper.pedido;

import com.gabriel.party.dtos.pedido.OrcamentoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoResponseDTO;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.prestador.Prestador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "valor", ignore = true)
    @Mapping(target = "detalhesOrcamento", ignore = true)
    @Mapping(target = "validadeOrcamento", ignore = true)
    @Mapping(target = "dataHoraCriacao", ignore = true)
    @Mapping(target = "dataHoraAtualizacao", ignore = true)
    @Mapping(target = "statusPedido", constant = "PENDENTE")
    @Mapping(source = "cliente", target = "cliente")
    @Mapping(source = "prestador", target = "prestador")
    @Mapping(source = "dto.descricao", target = "descricao")
    Pedido toEntity(PedidoRequestDTO dto, Cliente cliente, Prestador prestador);

    @Mapping(source = "cliente.nomeCompleto", target = "nomeCliente")
    @Mapping(source = "cliente.fotoPerfilUrl", target = "fotoClienteUrl")
    PedidoResponseDTO toResponseDTO(Pedido pedido);

    List<PedidoResponseDTO> toResponseList(List<Pedido> pedidos);

    void updatePedidoFromOrcamento(OrcamentoRequestDTO dto, @MappingTarget Pedido pedido);
}
