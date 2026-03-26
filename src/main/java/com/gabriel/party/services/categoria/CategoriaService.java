package com.gabriel.party.services.categoria;


import com.gabriel.party.dtos.categoria.CategoriaReponseDTO;
import com.gabriel.party.dtos.categoria.CategoriaRequestDTO;
import com.gabriel.party.exceptions.RecursoDuplicadoException;
import com.gabriel.party.exceptions.RecursoNaoEncontradoException;
import com.gabriel.party.exceptions.RegraNegocioException;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import jakarta.validation.Valid;
import com.gabriel.party.mapper.categoria.CategoriaMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final CategoriaMapper mapper;

    public CategoriaService(CategoriaRepository repository, CategoriaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public CategoriaReponseDTO salvarCategoria(CategoriaRequestDTO dto) {

            var categoriaExiste = repository.existsByNomeIgnoreCase(dto.nome());

            if (categoriaExiste){
                throw new RecursoDuplicadoException("Categoria com nome " + dto.nome() + " já existe.");
            }

            var novaCategoria = mapper.toEntity(dto);
            repository.save(novaCategoria);

            return mapper.toDto(novaCategoria);
    }

    @Transactional
    public void deletar(UUID id) {

        var categoria = repository.findByIdAndAtivoTrue(id) // Verificar se a categoria existe e está ativa
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com id: " + id));

        // Verificar se a categoria tem prestadores associados
        var categoriaTemPrestadores = categoria.getPrestadores() != null && !categoria.getPrestadores().isEmpty();

        if (categoriaTemPrestadores){
            throw new RegraNegocioException("Não é possível deletar a categoria, pois existem prestadores associados a ela.");
        }

        categoria.setAtivo(false);
        repository.save(categoria);
    }

    @Transactional(readOnly = true)
    public Page<CategoriaReponseDTO> listarCategorias(Pageable pageable) {

        return repository.findAllByAtivoTrue(pageable)
                .map(mapper::toDto);
    }

    @Transactional
    public CategoriaReponseDTO atualizarCategoria(@Valid CategoriaRequestDTO dto, UUID id) {

        var categoria = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com id: " + id));

        mapper.atualizarCategoriaDoDTO(dto, categoria);
         repository.save(categoria);
         return mapper.toDto(categoria);
    }

    @Transactional(readOnly = true)
    public CategoriaReponseDTO buscarCategoriaPorId(UUID id) {

        var categoria = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com id: " + id));

        return mapper.toDto(categoria);
    }
}
