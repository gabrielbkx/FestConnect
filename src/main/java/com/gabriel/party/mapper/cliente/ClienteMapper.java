package com.gabriel.party.mapper.cliente;

import com.gabriel.party.dtos.cliente.ClienteAdminDTO;
import com.gabriel.party.dtos.cliente.ClienteRequestDTO;
import com.gabriel.party.dtos.cliente.ClienteResponseDTO;
import com.gabriel.party.model.cliente.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClienteMapper {

    Cliente toEntity(ClienteRequestDTO dto);

    @Mapping(target = "nome", source = "nomeCompleto")
    @Mapping(target = "email", source = "usuario.email")
    ClienteResponseDTO toDto(Cliente cliente);

    // Mapeamento administrativo — endereço completo + data de criação.
    // Usado SOMENTE em /clientes/{id}/admin restrito a ROLE_ADMINISTRADOR.
    @Mapping(target = "nome", source = "nomeCompleto")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "dataCriacao", source = "usuario.dataCriacao")
    ClienteAdminDTO toAdminDto(Cliente cliente);

    void updateEntityFromDto(ClienteRequestDTO dto, @MappingTarget Cliente cliente);

}

