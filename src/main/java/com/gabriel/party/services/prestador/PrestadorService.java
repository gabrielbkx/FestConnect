package com.gabriel.party.services.prestador;


import com.gabriel.party.dtos.prestador.PrestadorRequestDTO;
import com.gabriel.party.dtos.prestador.PrestadorResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.prestador.PrestadorMapper;
import com.gabriel.party.model.prestador.Prestador;
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
    private final CategoriaRepository categoriaRepository;
    private final PrestadorMapper mapper;
    private final GeocodingService geocodingService;
    private final Logger logger = Logger.getLogger(PrestadorService.class.getName());

    public PrestadorService(PrestadorRepository repository, CategoriaRepository categoriaRepository, PrestadorMapper mapper, GeocodingService geocodingService) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.mapper = mapper;
        this.geocodingService = geocodingService;
    }

    @Transactional
    public PrestadorResponseDTO salvarPrestador(PrestadorRequestDTO dto) {

        if (repository.existsByEmailIgnoreCase(dto.email())) {
            throw new AppException(ErrorCode.PRESTADOR_EMAIL_DUPLICADO, dto.email());
        }

        var categoria = categoriaRepository.findByIdAndAtivoTrue(dto.categoriaId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORIA_NAO_ENCONTRADA, dto.categoriaId().toString()));

        var novoPrestador = mapper.toEntity(dto);
        novoPrestador.setCategoria(categoria);

        // vamos buscar as coordenadas do endereço para salvar junto com o prestador, assim já fica pronto para calcular o raio de atendimento
        String rua = dto.endereco().logradouro();
        String cidade = dto.endereco().cidade();
        String estado = dto.endereco().estado();
        var coordenadas = geocodingService.buscarCoordenadas(rua, cidade, estado);

        if (coordenadas != null) {
            novoPrestador.getEndereco().atribuirCoordenadas(coordenadas.latitude(), coordenadas.longitude());

        } else {
            logger.warning("Não foi possível obter coordenadas para o endereço do prestador: " + dto.endereco());
            throw new RuntimeException("Não é possivel salvar um novo prestador sem coordenadas de endereço."); // regra de negócio:
            // não faz sentido ter um prestador sem coordenadas, porque não tem como calcular o raio de atendimento.
            // Então a gente decide que se não conseguir as coordenadas, a gente nem salva o prestador.
        }

        repository.save(novoPrestador);

        return mapper.toDto(novoPrestador);
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
