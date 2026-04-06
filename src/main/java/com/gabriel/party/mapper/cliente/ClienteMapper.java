package com.gabriel.party.mapper.cliente;

import com.gabriel.party.dtos.cliente.ClienteRequestDTO;
import com.gabriel.party.dtos.cliente.ClienteResponseDTO;
import com.gabriel.party.model.cliente.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClienteMapper {

    Cliente toEntity(ClienteRequestDTO dto);

    ClienteResponseDTO toDto(Cliente cliente);

    void updateEntityFromDto(ClienteRequestDTO dto, @MappingTarget Cliente cliente);

}

