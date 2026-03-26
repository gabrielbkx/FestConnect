package com.gabriel.party.services.prestador;


import com.gabriel.party.dtos.prestador.PrestadorRequestDTO;
import com.gabriel.party.dtos.prestador.PrestadorResponseDTO;
import com.gabriel.party.exceptions.RecursoDuplicadoException;
import com.gabriel.party.exceptions.RecursoNaoEncontradoException;
import com.gabriel.party.mapper.prestador.PrestadorMapper;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PrestadorService {

    private final PrestadorRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final PrestadorMapper mapper;

    public PrestadorService(PrestadorRepository repository, CategoriaRepository categoriaRepository, PrestadorMapper mapper) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.mapper = mapper;
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
}
