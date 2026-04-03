package com.gabriel.party.services.cliente;

import com.gabriel.party.dtos.cliente.ClienteRequestDTO;
import com.gabriel.party.dtos.cliente.ClienteResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.cliente.ClienteMapper;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ClienteMapper mapper;

    @Transactional
    public ClienteResponseDTO criar(ClienteRequestDTO dto) {
        if (clienteRepository.existsByEmail(dto.email())) {
            throw new AppException(ErrorCode.CLIENTE_EMAIL_DUPLICADO, "O email informado já está em uso", dto.email());
        }

        Cliente cliente = mapper.toEntity(dto);
        cliente.setAtivo(true);
        cliente = clienteRepository.save(cliente);

        return mapper.toDto(cliente);
    }

    public ClienteResponseDTO buscarPorId(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, "Cliente não encontrado", id.toString()));
        return mapper.toDto(cliente);
    }

    public Page<ClienteResponseDTO> listarTodos(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional
    public ClienteResponseDTO atualizar(UUID id, ClienteRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, "Cliente não encontrado", id.toString()));

        if (!cliente.getEmail().equals(dto.email()) && clienteRepository.existsByEmail(dto.email())) {
            throw new AppException(ErrorCode.CLIENTE_EMAIL_DUPLICADO, "O email informado já está em uso", dto.email());
        }

        mapper.updateEntityFromDto(dto, cliente);
        cliente = clienteRepository.save(cliente);

        return mapper.toDto(cliente);
    }

    @Transactional
    public void deletar(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, "Cliente não encontrado", id.toString()));
        clienteRepository.delete(cliente);
    }
}

