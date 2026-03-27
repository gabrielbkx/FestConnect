package com.gabriel.party.services.prestador;


import com.gabriel.party.dtos.prestador.PrestadorRequestDTO;
import com.gabriel.party.dtos.prestador.PrestadorResponseDTO;
import com.gabriel.party.exceptions.RecursoDuplicadoException;
import com.gabriel.party.exceptions.RecursoNaoEncontradoException;
import com.gabriel.party.mapper.prestador.PrestadorMapper;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.integracoes.geocoding.GeocodingService;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
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
            throw new RecursoDuplicadoException("Prestador com e-mail " + dto.email() + " já existe.");
        }

        var categoria = categoriaRepository.findByIdAndAtivoTrue(dto.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com id: " + dto.categoriaId()));

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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador não encontrado com id: " + id));
        return mapper.toDto(prestador);
    }

    @Transactional
    public PrestadorResponseDTO atualizarPrestador(@Valid PrestadorRequestDTO dto, UUID id) {
        var prestador = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador não encontrado com id: " + id));

        var categoria = categoriaRepository.findByIdAndAtivoTrue(dto.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com id: " + dto.categoriaId()));

        mapper.atualizarPrestadorDoDTO(dto, prestador);
        prestador.setCategoria(categoria);
        repository.save(prestador);

        return mapper.toDto(prestador);
    }

    @Transactional
    public void deletar(UUID id) {
        var prestador = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador não encontrado com id: " + id));
        prestador.setAtivo(false);
        repository.save(prestador);
    }

    public List<PrestadorResponseDTO> buscarPrestadoresProximos(Double lat, Double lon, Double raio) {

        List<Prestador> prestadores = repository.buscarPorProximidade(lat, lon, raio);

        return prestadores.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
