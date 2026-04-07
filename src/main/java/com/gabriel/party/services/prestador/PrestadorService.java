package com.gabriel.party.services.prestador;


import com.gabriel.party.dtos.autenticacao.cadastro.prestador.CadastroPrestadorDTO;
import com.gabriel.party.dtos.prestador.PrestadorRequestDTO;
import com.gabriel.party.dtos.prestador.PrestadorResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.autenticacao.UsuarioMapper;
import com.gabriel.party.mapper.prestador.PrestadorMapper;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.integracoes.geocoding.GeocodingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class PrestadorService {

    private final PrestadorRepository repository;
    private final UsuarioMapper usuarioMapper;
    private final CategoriaRepository categoriaRepository;
    private final PrestadorMapper mapper;
    private final GeocodingService geocodingService;
    private final UsuarioRepository usuarioRepository;
    private final Logger logger = Logger.getLogger(PrestadorService.class.getName());

    public PrestadorService(PrestadorRepository repository,
                            UsuarioMapper usuarioMapper,
                            CategoriaRepository categoriaRepository,
                            PrestadorMapper mapper,
                            GeocodingService geocodingService,
                            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioMapper = usuarioMapper;
        this.categoriaRepository = categoriaRepository;
        this.mapper = mapper;
        this.geocodingService = geocodingService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Prestador criarPerfilPrestador(CadastroPrestadorDTO dto, Usuario usuario) {

        var categoria = categoriaRepository.findByIdAndAtivoTrue(dto.categoriaId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORIA_NAO_ENCONTRADA, dto.categoriaId().toString()));


        var novoPrestador = usuarioMapper.toPrestador(dto);
        novoPrestador.setCategoria(categoria);
        novoPrestador.setUsuario(usuario);


        String rua = dto.endereco().logradouro();
        String cidade = dto.endereco().cidade();
        String estado = dto.endereco().estado();
        var coordenadas = geocodingService.buscarCoordenadas(rua, cidade, estado);

        if (coordenadas != null) {
            novoPrestador.getEndereco().atribuirCoordenadas(coordenadas.latitude(), coordenadas.longitude());
        } else {
            logger.warning("Não foi possível obter coordenadas para o endereço do prestador: " + dto.endereco());
            throw new AppException(ErrorCode.REGRA_NEGOCIO_VIOLADA,
                    "Não é possível salvar um novo prestador sem coordenadas de endereço.");
        }

        return repository.save(novoPrestador);
    }

    @Transactional(readOnly = true)
    public Page<PrestadorResponseDTO> listarPrestadores(Pageable pageable) {
        return repository.findAllByAtivoTrue(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public PrestadorResponseDTO buscarPrestadorPorId(UUID id) {
        var prestador = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, id.toString()));
        return mapper.toDto(prestador);
    }

    @Transactional
    public PrestadorResponseDTO atualizarPrestador(@Valid PrestadorRequestDTO dto, UUID id) {
        var prestador = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, id.toString()));

        var categoria = categoriaRepository.findByIdAndAtivoTrue(dto.categoriaId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORIA_NAO_ENCONTRADA, dto.categoriaId().toString()));

        mapper.atualizarPrestadorDoDTO(dto, prestador);
        prestador.setCategoria(categoria);
        repository.save(prestador);

        return mapper.toDto(prestador);
    }

    @Transactional
    public void deletar(UUID id) {
        var prestador = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, id.toString()));

        prestador.setAtivo(false);
        prestador.getUsuario().setAtivo(false);
        usuarioRepository.save(prestador.getUsuario());
        repository.save(prestador);
    }

    @Transactional(readOnly = true)
    //Aqui mostrando todos os prestadores proximos sem filtro.
    //nos proximos passos vamos dar um jeito dele fazer um "top" prestadores mais proximos
    public List<PrestadorResponseDTO> buscarPrestadoresProximos(Double lat, Double lon, Double raio) {

        List<Prestador> prestadores = repository.buscarPorProximidade(lat, lon, raio);

        return prestadores.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrestadorResponseDTO> buscarPorFiltros(UUID categoriaId, Double lat, Double lon, Double raio) {
        // Define um raio padrão de 50km se vier nulo
        Double raioBusca = (raio != null) ? raio : 50.0;

        // Chama o Repository com a Query de Haversine + Categoria
        List<Prestador> prestadores = repository.buscarPorCategoriaEProximidade(categoriaId, lat, lon, raioBusca);

        // Converte a lista de entidades (com itens e mídias) para DTOs
        return prestadores.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
