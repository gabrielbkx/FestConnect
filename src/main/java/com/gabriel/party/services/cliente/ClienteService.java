package com.gabriel.party.services.cliente;

import com.gabriel.party.dtos.autenticacao.cadastro.cliente.CadastroClienteDTO;
import com.gabriel.party.dtos.cliente.ClienteRequestDTO;
import com.gabriel.party.dtos.cliente.ClienteResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.autenticacao.UsuarioMapper;
import com.gabriel.party.mapper.cliente.ClienteMapper;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.services.integracoes.geocoding.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper mapper;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final GeocodingService geocodingService;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository,
                          ClienteMapper mapper,
                          UsuarioMapper usuarioMapper,
                          UsuarioRepository usuarioRepository, GeocodingService geocodingService) {
        this.clienteRepository = clienteRepository;
        this.mapper = mapper;
        this.usuarioMapper = usuarioMapper;
        this.usuarioRepository = usuarioRepository;
        this.geocodingService = geocodingService;
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(UUID id) {
        Cliente cliente = clienteRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, "Cliente não encontrado", id.toString()));
        return mapper.toDto(cliente);
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listarTodos(Pageable pageable) {
        return clienteRepository.findAllByAtivoTrue(pageable).map(mapper::toDto);
    }

    @Transactional
    public ClienteResponseDTO atualizar(UUID id, ClienteRequestDTO dto) {

        Cliente cliente = clienteRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, "Cliente não encontrado", id.toString()));

        UUID idUsuarioAtual = cliente.getUsuario().getId();

        usuarioRepository.findByEmail(dto.email()).ifPresent(usuarioExistente -> {
            if (!usuarioExistente.getId().equals(idUsuarioAtual)) {
                throw new AppException(ErrorCode.CLIENTE_EMAIL_DUPLICADO, "O email informado já está em uso", dto.email());
            }
        });

        mapper.updateEntityFromDto(dto, cliente);
        cliente = clienteRepository.save(cliente);

        return mapper.toDto(cliente);
    }

    @Transactional
    public void deletar(UUID id) {

        Cliente cliente = clienteRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, "Cliente não encontrado", id.toString()));
        cliente.setAtivo(false);
        cliente.getUsuario().setAtivo(false);

        usuarioRepository.save(cliente.getUsuario());
        clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente criarPerfilCliente(CadastroClienteDTO dto, Usuario usuario) {

        Cliente novoCliente = usuarioMapper.toCliente(dto);

        novoCliente.setAtivo(true);
        novoCliente.setUsuario(usuario);

        String rua = dto.endereco().logradouro();
        String cidade = dto.endereco().cidade();
        String estado = dto.endereco().estado();

        var coordenadas = geocodingService.buscarCoordenadas(rua, cidade, estado);

        if (coordenadas != null) {
            novoCliente.getEndereco().atribuirCoordenadas(coordenadas.latitude(), coordenadas.longitude());
        }

       return clienteRepository.save(novoCliente);
    }
}
