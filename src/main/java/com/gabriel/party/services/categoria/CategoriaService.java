package com.gabriel.party.services.categoria;


import com.gabriel.party.dtos.categoria.CategoriaReponseDTO;
import com.gabriel.party.dtos.categoria.CategoriaRequestDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.model.categoria.Categoria;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
import jakarta.validation.Valid;
import com.gabriel.party.mapper.categoria.CategoriaMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final CategoriaMapper mapper;

    public CategoriaService(CategoriaRepository repository,
                            ItemCatalogoRepository itemCatalogoRepository,
                            CategoriaMapper mapper) {
        this.repository = repository;
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.mapper = mapper;
    }

    @Transactional
    public CategoriaReponseDTO salvarCategoria(CategoriaRequestDTO dto) {

            var categoriaExiste = repository.existsByNomeIgnoreCase(dto.nome());

            if (categoriaExiste){
                throw new AppException(ErrorCode.CATEGORIA_NOME_DUPLICADO, dto.nome());
            }

            var novaCategoria = mapper.toEntity(dto);
            repository.save(novaCategoria);

            return mapper.toDto(novaCategoria);
    }

    @Transactional
    public void deletar(UUID id) {

        var categoria = repository.findByIdAndAtivoTrue(id) // Verificar se a categoria existe e está ativa
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORIA_NAO_ENCONTRADA, id.toString()));

        // Não pode excluir se há itens de catálogo ativos vinculados a esta categoria
        if (itemCatalogoRepository.existsByCategoriaIdAndAtivoTrue(id)) {
            throw new AppException(ErrorCode.REGRA_NEGOCIO_VIOLADA, ("A categoria '%categoria%'" +
                    " não pode ser excluída pois existem itens vinculados a ela.")
                    .replace("%categoria%", categoria.getNome()));
        }

        categoria.setAtivo(false);
        repository.save(categoria);
    }

    @Transactional(readOnly = true)
    public Page<CategoriaReponseDTO> listarCategorias(Pageable pageable) {
        Page<Categoria> pagina = repository.findAllByAtivoTrue(pageable);

        List<UUID> ids = pagina.getContent().stream().map(Categoria::getId).toList();
        // Uma consulta só para a página inteira; categorias sem prestador nem aparecem no resultado (default 0).
        Map<UUID, Integer> prestadoresPorCategoria = ids.isEmpty()
                ? Map.of()
                : itemCatalogoRepository.contarPrestadoresPorCategoria(ids).stream()
                        .collect(Collectors.toMap(
                                ItemCatalogoRepository.ContagemDePrestadoresPorCategoria::getCategoriaId,
                                c -> (int) c.getTotal()));

        return pagina.map(categoria ->
                mapper.toDto(categoria, prestadoresPorCategoria.getOrDefault(categoria.getId(), 0)));
    }

    @Transactional
    public CategoriaReponseDTO atualizarCategoria(@Valid CategoriaRequestDTO dto, UUID id) {

        var categoria = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORIA_NAO_ENCONTRADA, id.toString()));

        var categoriaComMesmoNome = repository.findByNomeIgnoreCaseAndAtivoTrue(dto.nome())
                .filter(c -> !c.getId().equals(id))
                .isPresent();

        if (categoriaComMesmoNome) {
            throw new AppException(ErrorCode.CATEGORIA_NOME_DUPLICADO, dto.nome());
        }

        mapper.atualizarCategoriaDoDTO(dto, categoria);
         repository.save(categoria);
         return mapper.toDto(categoria);
    }

    @Transactional(readOnly = true)
    public CategoriaReponseDTO buscarCategoriaPorId(UUID id) {

        var categoria = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORIA_NAO_ENCONTRADA, id.toString()));

        return mapper.toDto(categoria);
    }
}
