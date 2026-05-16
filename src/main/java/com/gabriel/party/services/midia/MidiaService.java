package com.gabriel.party.services.midia;

import com.gabriel.party.dtos.midia.MidiaRequestDTO;
import com.gabriel.party.dtos.midia.MidiaResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.midia.MidiaMapper;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.midia.enums.TipoMidia;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
import com.gabriel.party.repositories.midia.MidiaRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.integracoes.aws.ArmazenamentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class MidiaService {

    private final MidiaRepository repository;
    private final PrestadorRepository prestadorRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final MidiaMapper mapper;
    private final ArmazenamentoService armazenamentoService;

    public MidiaService(MidiaRepository repository,
                        PrestadorRepository prestadorRepository,
                        ItemCatalogoRepository itemCatalogoRepository,
                        MidiaMapper mapper,
                        ArmazenamentoService armazenamentoService) {
        this.repository = repository;
        this.prestadorRepository = prestadorRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.mapper = mapper;
        this.armazenamentoService = armazenamentoService;
    }

    @Transactional
    public MidiaResponseDTO salvarMidia(MultipartFile arquivo, MidiaRequestDTO dto, UUID usuarioId) {
        var prestador = prestadorRepository.findByUsuarioIdAndAtivoTrue(usuarioId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, usuarioId.toString()));

        if (repository.countMidiaByPrestadorId(prestador.getId()) >= 30) {
            throw new AppException(ErrorCode.LIMITE_MIDIAS_PRESTADOR, prestador.getId().toString());
        }

        ItemCatalogo item = itemCatalogoRepository.findByIdAndAtivoTrue(dto.itemCatalogoId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO, dto.itemCatalogoId().toString()));
        if (!item.getPrestador().getId().equals(prestador.getId())) {
            throw new AppException(ErrorCode.USUARIO_SEM_PERMISSAO, usuarioId.toString());
        }

        var novaMidia = mapper.toEntity(dto);
        novaMidia.setItemCatalogo(item);
        novaMidia.setTipo(deduzirTipo(arquivo));
        novaMidia.setUrl(armazenamentoService.salvarMidias(arquivo));

        try {
            repository.save(novaMidia);
        } catch (Exception e) {
            armazenamentoService.deletaMidia(novaMidia.getUrl());
            throw new AppException(ErrorCode.ERRO_SALVAR_MIDIA, e.getMessage());
        }

        return mapper.toDto(novaMidia);
    }

    @Transactional(readOnly = true)
    public Page<MidiaResponseDTO> listarMidias(Pageable pageable, UUID prestadorId, TipoMidia tipoMidia) {
        return repository.buscarTodasAsMidiasDoPrestador(pageable, prestadorId, tipoMidia).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public MidiaResponseDTO buscarMidiaPorId(UUID id) {
        var midia = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MIDIA_NAO_ENCONTRADA, id.toString()));
        return mapper.toDto(midia);
    }

    @Transactional
    public MidiaResponseDTO atualizarMidia(@Valid MidiaRequestDTO dto, UUID id, UUID usuarioId) {
        var midia = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MIDIA_NAO_ENCONTRADA, id.toString()));

        var prestador = prestadorRepository.findByUsuarioIdAndAtivoTrue(usuarioId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, usuarioId.toString()));

        if (!midia.getItemCatalogo().getPrestador().getId().equals(prestador.getId())) {
            throw new AppException(ErrorCode.USUARIO_SEM_PERMISSAO, usuarioId.toString());
        }

        mapper.atualizarMidiaDoDTO(dto, midia);

        if (dto.itemCatalogoId() != null) {
            var item = itemCatalogoRepository.findByIdAndAtivoTrue(dto.itemCatalogoId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO, dto.itemCatalogoId().toString()));
            if (!item.getPrestador().getId().equals(prestador.getId())) {
                throw new AppException(ErrorCode.USUARIO_SEM_PERMISSAO, usuarioId.toString());
            }
            midia.setItemCatalogo(item);
        }

        repository.save(midia);
        return mapper.toDto(midia);
    }

    private TipoMidia deduzirTipo(MultipartFile arquivo) {
        String contentType = arquivo.getContentType();
        if (contentType == null) {
            throw new AppException(ErrorCode.FORMATO_INVALIDO, "desconhecido");
        }
        if (contentType.startsWith("image/")) return TipoMidia.FOTO;
        if (contentType.startsWith("video/")) return TipoMidia.VIDEO;
        throw new AppException(ErrorCode.FORMATO_INVALIDO, contentType);
    }

    @Transactional
    public void deletar(UUID id, UUID usuarioId) {
        var midia = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MIDIA_NAO_ENCONTRADA, id.toString()));

        var prestador = prestadorRepository.findByUsuarioIdAndAtivoTrue(usuarioId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, usuarioId.toString()));

        if (!midia.getItemCatalogo().getPrestador().getId().equals(prestador.getId())) {
            throw new AppException(ErrorCode.USUARIO_SEM_PERMISSAO, usuarioId.toString());
        }

        armazenamentoService.deletaMidia(midia.getUrl());
        repository.delete(midia);
    }
}
