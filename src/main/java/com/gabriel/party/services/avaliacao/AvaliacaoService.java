package com.gabriel.party.services.avaliacao;


import com.gabriel.party.dtos.avaliacao.AvaliacaoCreateDTO;
import com.gabriel.party.dtos.avaliacao.AvaliacaoRequestDTO;
import com.gabriel.party.dtos.avaliacao.AvaliacaoResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.avaliacao.AvaliacaoMapper;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.avaliacao.AvaliacaoRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import jakarta.validation.Valid;
import liquibase.ui.UIService;
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
    private final ClienteRepository clienteRepository;

    public AvaliacaoService(AvaliacaoRepository repository, PrestadorRepository prestadorRepository, AvaliacaoMapper mapper, ClienteRepository clienteRepository) {
        this.repository = repository;
        this.prestadorRepository = prestadorRepository;
        this.mapper = mapper;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public AvaliacaoResponseDTO salvarAvaliacao(AvaliacaoCreateDTO dto, UUID usuarioId) {

        var cliente = clienteRepository.findByUsuarioIdAndAtivoTrue((usuarioId))
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, usuarioId.toString()));

        var prestador = prestadorRepository.findByIdAndAtivoTrue(dto.prestadorId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, dto.prestadorId().toString()));

        var novaAvaliacao = mapper.toEntity(dto);
        novaAvaliacao.setPrestador(prestador);
        novaAvaliacao.setCliente(cliente);
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
                .orElseThrow(() -> new AppException(ErrorCode.AVALIACAO_NAO_ENCONTRADA, id.toString()));
        return mapper.toDto(avaliacao);
    }

    @Transactional
    public AvaliacaoResponseDTO atualizarAvaliacao(@Valid AvaliacaoRequestDTO dto, UUID idAvaliacao, UUID usuarioId) {

        var cliente = clienteRepository.findByUsuarioIdAndAtivoTrue((usuarioId))
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, usuarioId.toString()));

        var avaliacao = repository.findByIdAndAtivoTrue(idAvaliacao)
                .orElseThrow(() -> new AppException(ErrorCode.AVALIACAO_NAO_ENCONTRADA, idAvaliacao.toString()));

        var clienteId = cliente.getId();
        var avaliacaoClienteId = avaliacao.getCliente().getId();

        if (!clienteId.equals(avaliacaoClienteId)) {
            throw new AppException(ErrorCode.AVALIACAO_NAO_PERTENCE_AO_CLIENTE_MENCIONADO, idAvaliacao.toString()
                    , clienteId.toString());
        }

        mapper.atualizarAvaliacaoDoDTO(dto, avaliacao);
        repository.save(avaliacao);

        return mapper.toDto(avaliacao);
    }

    @Transactional
    public void deletar(UUID id, Usuario usuario) {

        var avaliacao = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.AVALIACAO_NAO_ENCONTRADA, id.toString()));

        String role = usuario.getRole().name();

        if (role.equalsIgnoreCase("ROLE_ADMINISTRADOR")){
            avaliacao.setAtivo(false);
            repository.save(avaliacao);
            return;
        }

        var cliente = clienteRepository.findByUsuarioIdAndAtivoTrue((usuario.getId()))
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, usuario.getId().toString()));

        if (!cliente.getId().equals(avaliacao.getCliente().getId())){
            throw new AppException(ErrorCode.AVALIACAO_NAO_PERTENCE_AO_CLIENTE_MENCIONADO, id.toString()
                    , cliente.getId().toString());
        }

        avaliacao.setAtivo(false);
        repository.save(avaliacao);
    }
}
