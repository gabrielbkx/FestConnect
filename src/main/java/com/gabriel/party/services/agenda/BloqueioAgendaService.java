package com.gabriel.party.services.agenda;

import com.gabriel.party.dtos.agenda.BloqueioRequestDTO;
import com.gabriel.party.dtos.agenda.BloqueioResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.model.agenda.BloqueioAgenda;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.agenda.BloqueioAgendaRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BloqueioAgendaService {

    private final BloqueioAgendaRepository bloqueioRepository;
    private final PrestadorRepository prestadorRepository;

    public BloqueioAgendaService(BloqueioAgendaRepository bloqueioRepository, PrestadorRepository prestadorRepository) {
        this.bloqueioRepository = bloqueioRepository;
        this.prestadorRepository = prestadorRepository;
    }

    @Transactional
    public BloqueioResponseDTO criar(BloqueioRequestDTO dto, Usuario usuarioLogado) {
        Prestador prestador = prestadorDoUsuario(usuarioLogado);
        if (!dto.fim().isAfter(dto.inicio())) {
            throw new AppException(ErrorCode.BLOQUEIO_INTERVALO_INVALIDO);
        }

        var bloqueio = new BloqueioAgenda();
        bloqueio.setPrestador(prestador);
        bloqueio.setInicio(dto.inicio());
        bloqueio.setFim(dto.fim());
        bloqueio.setMotivo(dto.motivo());

        return toDto(bloqueioRepository.save(bloqueio));
    }

    @Transactional(readOnly = true)
    public List<BloqueioResponseDTO> listarMeus(Usuario usuarioLogado) {
        Prestador prestador = prestadorDoUsuario(usuarioLogado);
        return bloqueioRepository.findByPrestadorIdOrderByInicio(prestador.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void excluir(UUID bloqueioId, Usuario usuarioLogado) {
        Prestador prestador = prestadorDoUsuario(usuarioLogado);
        BloqueioAgenda bloqueio = bloqueioRepository.findById(bloqueioId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOQUEIO_NAO_ENCONTRADO, bloqueioId.toString()));
        if (!bloqueio.getPrestador().getId().equals(prestador.getId())) {
            throw new AppException(ErrorCode.BLOQUEIO_SEM_PERMISSAO);
        }
        bloqueioRepository.delete(bloqueio);
    }

    private Prestador prestadorDoUsuario(Usuario usuarioLogado) {
        return prestadorRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO));
    }

    private BloqueioResponseDTO toDto(BloqueioAgenda b) {
        return new BloqueioResponseDTO(b.getId(), b.getInicio(), b.getFim(), b.getMotivo());
    }
}
