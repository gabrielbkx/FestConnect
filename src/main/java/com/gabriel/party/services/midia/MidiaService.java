package com.gabriel.party.services.midia;


import com.gabriel.party.dtos.midia.MidiaRequestDTO;
import com.gabriel.party.dtos.midia.MidiaResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.midia.MidiaMapper;
import com.gabriel.party.model.midia.enums.TipoMidia;
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
    private final MidiaMapper mapper;
    private final ArmazenamentoService armazenamentoService;

    public MidiaService(MidiaRepository repository, PrestadorRepository prestadorRepository, MidiaMapper mapper, ArmazenamentoService armazenamentoService) {
        this.repository = repository;
        this.prestadorRepository = prestadorRepository;
        this.mapper = mapper;
        this.armazenamentoService = armazenamentoService;
    }

    @Transactional
    public MidiaResponseDTO salvarMidia(MultipartFile arquivo, MidiaRequestDTO dto) {
        var prestador = prestadorRepository.findByIdAndAtivoTrue(dto.prestadorId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, dto.prestadorId().toString()));


        var totalMidiasDoPrestador = repository.countByPrestadorId(prestador.getId());
        if (totalMidiasDoPrestador >= 10) {
            throw new AppException(ErrorCode.LIMITE_MIDIAS_PRESTADOR, prestador.getId().toString());
        }
        var novaMidia = mapper.toEntity(dto);
        novaMidia.setPrestador(prestador);
        novaMidia.setUrl(armazenamentoService.salvarMidias(arquivo));

        // caso aconteca algum erro ao salvar a url da midia no banoc de dados, nos apagamos esse registro
        // "fantasma" do bucket do s3
        try {
            repository.save(novaMidia);
        }catch ( Exception e) {
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
    public MidiaResponseDTO atualizarMidia(@Valid MidiaRequestDTO dto, UUID id) {
        var midia = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MIDIA_NAO_ENCONTRADA, id.toString()));

        var prestador = prestadorRepository.findByIdAndAtivoTrue(dto.prestadorId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, id.toString()));

        mapper.atualizarMidiaDoDTO(dto, midia);
        midia.setPrestador(prestador);
        repository.save(midia);

        return mapper.toDto(midia);
    }

    @Transactional
    public void deletar(UUID id) {

        var midia = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MIDIA_NAO_ENCONTRADA, id.toString()));
        var nomeArquivo = midia.getUrl();
        armazenamentoService.deletaMidia(nomeArquivo);
        repository.save(midia);
    }
}
