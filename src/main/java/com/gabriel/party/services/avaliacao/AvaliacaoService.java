package com.gabriel.party.services.avaliacao;


import com.gabriel.party.dtos.avaliacao.AvaliacaoRequestDTO;
import com.gabriel.party.dtos.avaliacao.AvaliacaoResponseDTO;
import com.gabriel.party.exceptions.RecursoNaoEncontradoException;
import com.gabriel.party.mapper.avaliacao.AvaliacaoMapper;
import com.gabriel.party.repositories.avaliacao.AvaliacaoRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository repository;
    private final PrestadorRepository prestadorRepository;
    private final AvaliacaoMapper mapper;

    public AvaliacaoService(AvaliacaoRepository repository, PrestadorRepository prestadorRepository, AvaliacaoMapper mapper) {
        this.repository = repository;
        this.prestadorRepository = prestadorRepository;
        this.mapper = mapper;
    }

    @Transactional
    public AvaliacaoResponseDTO salvarAvaliacao(AvaliacaoRequestDTO dto) {
        var prestador = prestadorRepository.findByIdAndAtivoTrue(dto.prestadorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador não encontrado com id: " + dto.prestadorId()));

        var novaAvaliacao = mapper.toEntity(dto);
        novaAvaliacao.setPrestador(prestador);
        repository.save(novaAvaliacao);

        return mapper.toDto(novaAvaliacao);
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoResponseDTO> listarAvaliacoes(Pageable pageable) {
        return repository.findAllByAtivoTrue(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponseDTO buscarAvaliacaoPorId(UUID id) {
        var avaliacao = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliacao não encontrada com id: " + id));
        return mapper.toDto(avaliacao);
    }

    @Transactional
    public AvaliacaoResponseDTO atualizarAvaliacao(@Valid AvaliacaoRequestDTO dto, UUID id) {
        var avaliacao = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliacao não encontrada com id: " + id));

        var prestador = prestadorRepository.findByIdAndAtivoTrue(dto.prestadorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador não encontrado com id: " + dto.prestadorId()));

        mapper.atualizarAvaliacaoDoDTO(dto, avaliacao);
        avaliacao.setPrestador(prestador);
        repository.save(avaliacao);

        return mapper.toDto(avaliacao);
    }

    @Transactional
    public void deletar(UUID id) {
        var avaliacao = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliacao não encontrada com id: " + id));
        avaliacao.setAtivo(false);
        repository.save(avaliacao);
    }
}
