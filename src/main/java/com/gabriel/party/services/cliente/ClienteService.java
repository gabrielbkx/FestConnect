package com.gabriel.party.services.cliente;

import com.gabriel.party.dtos.autenticacao.cadastro.cliente.CadastroClienteDTO;
import com.gabriel.party.dtos.categoria.CategoriaReponseDTO;
import com.gabriel.party.dtos.cliente.ClienteRequestDTO;
import com.gabriel.party.dtos.cliente.ClienteResponseDTO;
import com.gabriel.party.dtos.prestador.PrestadorResumoDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.autenticacao.UsuarioMapper;
import com.gabriel.party.mapper.categoria.CategoriaMapper;
import com.gabriel.party.mapper.cliente.ClienteMapper;
import com.gabriel.party.mapper.prestador.PrestadorMapper;
import com.gabriel.party.model.categoria.Categoria;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.integracoes.aws.ArmazenamentoService;
import com.gabriel.party.services.integracoes.geocoding.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper mapper;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final GeocodingService geocodingService;
    private final ArmazenamentoService armazenamentoService;
    private final PrestadorRepository prestadorRepository;
    private final CategoriaRepository categoriaRepository;
    private final PrestadorMapper prestadorMapper;
    private final CategoriaMapper categoriaMapper;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository,
                          ClienteMapper mapper,
                          UsuarioMapper usuarioMapper,
                          UsuarioRepository usuarioRepository,
                          GeocodingService geocodingService,
                          ArmazenamentoService armazenamentoService,
                          PrestadorRepository prestadorRepository,
                          CategoriaRepository categoriaRepository,
                          PrestadorMapper prestadorMapper,
                          CategoriaMapper categoriaMapper) {
        this.clienteRepository = clienteRepository;
        this.mapper = mapper;
        this.usuarioMapper = usuarioMapper;
        this.usuarioRepository = usuarioRepository;
        this.geocodingService = geocodingService;
        this.armazenamentoService = armazenamentoService;
        this.prestadorRepository = prestadorRepository;
        this.categoriaRepository = categoriaRepository;
        this.prestadorMapper = prestadorMapper;
        this.categoriaMapper = categoriaMapper;
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
    public void adicionarPrestadorFavorito(UUID clienteId, UUID prestadorId) {
        Cliente cliente = clienteRepository.findByIdAndAtivoTrue(clienteId)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, clienteId.toString()));
        Prestador prestador = prestadorRepository.findByIdAndAtivoTrue(prestadorId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, prestadorId.toString()));

        if (cliente.getPrestadoresFavoritos() == null) {
            cliente.setPrestadoresFavoritos(new HashSet<>());
        }
        if (!cliente.getPrestadoresFavoritos().add(prestador)) {
            throw new AppException(ErrorCode.FAVORITO_JA_ADICIONADO, prestadorId.toString());
        }
        clienteRepository.save(cliente);
    }

    @Transactional
    public void removerPrestadorFavorito(UUID clienteId, UUID prestadorId) {
        Cliente cliente = clienteRepository.findByIdAndAtivoTrue(clienteId)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, clienteId.toString()));

        if (cliente.getPrestadoresFavoritos() == null
                || !cliente.getPrestadoresFavoritos().removeIf(p -> p.getId().equals(prestadorId))) {
            throw new AppException(ErrorCode.FAVORITO_NAO_ENCONTRADO, prestadorId.toString());
        }
        clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<PrestadorResumoDTO> listarPrestadoresFavoritos(UUID clienteId) {
        Cliente cliente = clienteRepository.findByIdAndAtivoTrue(clienteId)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, clienteId.toString()));

        if (cliente.getPrestadoresFavoritos() == null || cliente.getPrestadoresFavoritos().isEmpty()) {
            return List.of();
        }
        return prestadorMapper.toSummaryList(new ArrayList<>(cliente.getPrestadoresFavoritos()));
    }

    @Transactional
    public void adicionarCategoriaFavorita(UUID clienteId, UUID categoriaId) {
        Cliente cliente = clienteRepository.findByIdAndAtivoTrue(clienteId)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, clienteId.toString()));
        Categoria categoria = categoriaRepository.findByIdAndAtivoTrue(categoriaId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORIA_NAO_ENCONTRADA, categoriaId.toString()));

        if (cliente.getCategoriasFavoritas() == null) {
            cliente.setCategoriasFavoritas(new HashSet<>());
        }
        if (!cliente.getCategoriasFavoritas().add(categoria)) {
            throw new AppException(ErrorCode.FAVORITO_JA_ADICIONADO, categoriaId.toString());
        }
        clienteRepository.save(cliente);
    }

    @Transactional
    public void removerCategoriaFavorita(UUID clienteId, UUID categoriaId) {
        Cliente cliente = clienteRepository.findByIdAndAtivoTrue(clienteId)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, clienteId.toString()));

        if (cliente.getCategoriasFavoritas() == null
                || !cliente.getCategoriasFavoritas().removeIf(c -> c.getId().equals(categoriaId))) {
            throw new AppException(ErrorCode.FAVORITO_NAO_ENCONTRADO, categoriaId.toString());
        }
        clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<CategoriaReponseDTO> listarCategoriasFavoritas(UUID clienteId) {
        Cliente cliente = clienteRepository.findByIdAndAtivoTrue(clienteId)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, clienteId.toString()));

        if (cliente.getCategoriasFavoritas() == null || cliente.getCategoriasFavoritas().isEmpty()) {
            return List.of();
        }
        return cliente.getCategoriasFavoritas().stream().map(categoriaMapper::toDto).toList();
    }

    @Transactional
    public Cliente criarPerfilCliente(CadastroClienteDTO dto, Usuario usuario, MultipartFile fotoPerfil) {

        Cliente novoCliente = usuarioMapper.toCliente(dto);

        novoCliente.setAtivo(true);
        novoCliente.setFotoPerfilUrl(fotoPerfil != null ? armazenamentoService.salvarMidias(fotoPerfil) : null);
        novoCliente.setUsuario(usuario);

        if (dto.endereco() != null && novoCliente.getEndereco() != null) {
            String rua = dto.endereco().logradouro();
            String cidade = dto.endereco().cidade();
            String estado = dto.endereco().estado();

            var coordenadas = geocodingService.buscarCoordenadas(rua, cidade, estado);

            if (coordenadas != null) {
                novoCliente.getEndereco().atribuirCoordenadas(coordenadas.latitude(), coordenadas.longitude());
            }
        }

       return clienteRepository.save(novoCliente);
    }
}
