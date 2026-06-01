package com.gabriel.party.services.itemcatalogo;

import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoRequestDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResumoDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.itemcatalogo.ItemCatalogoMapper;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.repositories.avaliacao.AvaliacaoRepository;
import com.gabriel.party.repositories.avaliacao.EstatisticasItemAvaliacao;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ItemCatalogoService {

    private final ItemCatalogoRepository itemCatalogoRepository;
    private final PrestadorRepository prestadorRepository;
    private final CategoriaRepository categoriaRepository;
    private final ItemCatalogoMapper itemCatalogoMapper;
    private final AvaliacaoRepository avaliacaoRepository;

    public ItemCatalogoService(ItemCatalogoRepository itemCatalogoRepository,
            PrestadorRepository prestadorRepository,
            CategoriaRepository categoriaRepository,
            ItemCatalogoMapper itemCatalogoMapper,
            AvaliacaoRepository avaliacaoRepository) {
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.prestadorRepository = prestadorRepository;
        this.categoriaRepository = categoriaRepository;
        this.itemCatalogoMapper = itemCatalogoMapper;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Transactional
    public ItemCatalogoResponseDTO criarItem(ItemCatalogoRequestDTO dto, UUID usuarioId) {

        Prestador prestador = prestadorRepository.findByUsuarioIdAndAtivoTrue((usuarioId))
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, usuarioId.toString()));

        var categoria = categoriaRepository.findByIdAndAtivoTrue(dto.categoriaId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORIA_NAO_ENCONTRADA, dto.categoriaId().toString()));

        ItemCatalogo novoItem = itemCatalogoMapper.toEntity(dto);
        novoItem.setPrestador(prestador);
        novoItem.setCategoria(categoria);

        ItemCatalogo itemSalvo = itemCatalogoRepository.save(novoItem);
        return itemCatalogoMapper.toDto(itemSalvo);
    }

    @Transactional(readOnly = true)
    public List<ItemCatalogoResponseDTO> listarVitrineDoPrestador(UUID prestadorId) {
        // Retorna todos os itens daquele prestador já convertidos para a tela do app
        return itemCatalogoRepository.findAllByPrestadorIdAndAtivoTrue(prestadorId)
                .stream()
                .map(itemCatalogoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ItemCatalogoResponseDTO> listarItensCatalogo(Pageable pageable) {
        return itemCatalogoRepository.findAllByAtivoTrue(pageable).map(itemCatalogoMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ItemCatalogoResumoDTO> buscarItens(String busca, String cidade, List<UUID> categoriaIds,
            Pageable pageable) {
        String buscaTratada = busca == null ? "" : busca.trim();
        String cidadeTratada = cidade == null ? "" : cidade.trim();

        Page<ItemCatalogo> resultado = (categoriaIds == null || categoriaIds.isEmpty())
                ? itemCatalogoRepository.buscarSemCategoria(buscaTratada, cidadeTratada, pageable)
                : itemCatalogoRepository.buscarComCategorias(categoriaIds, buscaTratada, cidadeTratada, pageable);

        List<UUID> idsItens = resultado.getContent().stream().map(ItemCatalogo::getId).toList();

        Map<UUID, EstatisticasItemAvaliacao> estatisticasPorItem = idsItens.isEmpty() ? Map.of()
                : avaliacaoRepository.buscarEstatisticasPorItens(idsItens)
                        .stream()
                        .collect(Collectors.toMap(EstatisticasItemAvaliacao::getIdItem, e -> e));

        return resultado.map(item -> {
            ItemCatalogoResumoDTO resumo = itemCatalogoMapper.toResumoDto(item);
            EstatisticasItemAvaliacao estatisticas = estatisticasPorItem.get(item.getId());
            if (estatisticas == null) return resumo;
            return new ItemCatalogoResumoDTO(
                    resumo.id(), resumo.titulo(), resumo.precoBase(), resumo.tipo(),
                    resumo.categoriaId(), resumo.categoriaNome(),
                    resumo.prestadorId(), resumo.prestadorNome(),
                    resumo.cidade(), resumo.estado(), resumo.fotoPrincipalUrl(),
                    estatisticas.getMedia(), estatisticas.getTotal()
            );
        });
    }

    @Transactional(readOnly = true)
    public ItemCatalogoResponseDTO buscarItemPorId(UUID id) {
        var itemCatalogo = itemCatalogoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO, id.toString()));
        return itemCatalogoMapper.toDto(itemCatalogo);
    }

    @Transactional
    public ItemCatalogoResponseDTO atualizarItem(@Valid ItemCatalogoRequestDTO dto, UUID idItem, UUID usuarioId) {

        var itemCatalogo = itemCatalogoRepository.findByIdAndAtivoTrue(idItem)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO, idItem.toString()));

        var prestadorIdDoItem = itemCatalogo.getPrestador().getId();

        Prestador prestador = prestadorRepository.findByUsuarioIdAndAtivoTrue((usuarioId))
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, usuarioId.toString()));

        var prestadorId = prestador.getId();

        if (!prestadorId.equals(prestadorIdDoItem)) {
            throw new AppException(ErrorCode.USUARIO_SEM_PERMISSAO, usuarioId.toString());
        }

        itemCatalogoMapper.atualizarItemDoDTO(dto, itemCatalogo);

        if (dto.categoriaId() != null) {
            var categoria = categoriaRepository.findByIdAndAtivoTrue(dto.categoriaId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORIA_NAO_ENCONTRADA, dto.categoriaId().toString()));
            itemCatalogo.setCategoria(categoria);
        }

        itemCatalogoRepository.save(itemCatalogo);

        return itemCatalogoMapper.toDto(itemCatalogo);
    }

    @Transactional
    public void deletar(UUID id) {
        var itemCatalogo = itemCatalogoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO, id.toString()));
        itemCatalogo.setAtivo(false);
        itemCatalogoRepository.save(itemCatalogo);
    }

    @Transactional(readOnly = true)
    public Page<ItemCatalogoResponseDTO> buscarItensPorRadarEBusca(String termoBusca, String tipo, Double lat,
            Double lon, Double raio, Pageable pageable) {
        Double raioMaximo = (raio != null && raio <= 50.0) ? raio : 10.0;
        String termoTratado = (termoBusca == null || termoBusca.trim().isEmpty()) ? "" : termoBusca.trim();
        String tipoTratado = (tipo == null || tipo.trim().isEmpty()) ? "" : tipo.trim().toUpperCase();

        return itemCatalogoRepository
                .buscarItensPorTermoEProximidade(termoTratado, tipoTratado, lat, lon, raioMaximo, pageable)
                .map(itemCatalogoMapper::toDto);
    }
}