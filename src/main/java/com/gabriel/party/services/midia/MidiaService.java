package com.gabriel.party.services.midia;


import com.gabriel.party.dtos.midia.MidiaRequestDTO;
import com.gabriel.party.dtos.midia.MidiaResponseDTO;
import com.gabriel.party.exceptions.RecursoNaoEncontradoException;
import com.gabriel.party.mapper.midia.MidiaMapper;
import com.gabriel.party.repositories.midia.MidiaRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MidiaService {

    private final MidiaRepository repository;
    private final PrestadorRepository prestadorRepository;
    private final MidiaMapper mapper;

    public MidiaService(MidiaRepository repository, PrestadorRepository prestadorRepository, MidiaMapper mapper) {
        this.repository = repository;
        this.prestadorRepository = prestadorRepository;
        this.mapper = mapper;
    }

    @Transactional
    public MidiaResponseDTO salvarMidia(MidiaRequestDTO dto) {
        var prestador = prestadorRepository.findByIdAndAtivoTrue(dto.prestadorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador não encontrado com id: " + dto.prestadorId()));

        var novaMidia = mapper.toEntity(dto);
        novaMidia.setPrestador(prestador);
        repository.save(novaMidia);

        return mapper.toDto(novaMidia);
    }

    @Transactional(readOnly = true)
    public Page<MidiaResponseDTO> listarMidias(Pageable pageable) {
        return repository.findAllByAtivoTrue(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public MidiaResponseDTO buscarMidiaPorId(UUID id) {
        var midia = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Midia não encontrada com id: " + id));
        return mapper.toDto(midia);
    }

    @Transactional
    public MidiaResponseDTO atualizarMidia(@Valid MidiaRequestDTO dto, UUID id) {
        var midia = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Midia não encontrada com id: " + id));

        var prestador = prestadorRepository.findByIdAndAtivoTrue(dto.prestadorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador não encontrado com id: " + dto.prestadorId()));

        mapper.atualizarMidiaDoDTO(dto, midia);
        midia.setPrestador(prestador);
        repository.save(midia);

        return mapper.toDto(midia);
    }

    @Transactional
    public void deletar(UUID id) {
        var midia = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Midia não encontrada com id: " + id));
        midia.setAtivo(false);
        repository.save(midia);
    }
}
