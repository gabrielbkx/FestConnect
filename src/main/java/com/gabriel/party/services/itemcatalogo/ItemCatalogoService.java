package com.gabriel.party.services.itemcatalogo;

import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoRequestDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.itemcatalogo.ItemCatalogoMapper;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class ItemCatalogoService {

    private final ItemCatalogoRepository itemCatalogoRepository;
    private final PrestadorRepository prestadorRepository;
    private final ItemCatalogoMapper itemCatalogoMapper;

    public ItemCatalogoService(ItemCatalogoRepository itemCatalogoRepository,
                               PrestadorRepository prestadorRepository,
                               ItemCatalogoMapper itemCatalogoMapper) {
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.prestadorRepository = prestadorRepository;
        this.itemCatalogoMapper = itemCatalogoMapper;
    }

    @Transactional
    public ItemCatalogoResponseDTO criarItem(ItemCatalogoRequestDTO dto) {

        Prestador prestador = prestadorRepository.findByIdAndAtivoTrue(dto.prestadorId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, dto.prestadorId().toString()));

        ItemCatalogo novoItem = itemCatalogoMapper.toEntity(dto);

        // 3. Amarra o dono ao item
        novoItem.setPrestador(prestador);

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
    public ItemCatalogoResponseDTO buscarItemPorId(UUID id) {
        var itemCatalogo = itemCatalogoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO, id.toString()));
        return itemCatalogoMapper.toDto(itemCatalogo);
    }

    @Transactional
    public ItemCatalogoResponseDTO atualizarItem(@Valid ItemCatalogoRequestDTO dto, UUID id) {
        var itemCatalogo = itemCatalogoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO, id.toString()));

        var prestador = prestadorRepository.findByIdAndAtivoTrue(dto.prestadorId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, dto.prestadorId().toString()));

        itemCatalogoMapper.atualizarItemDoDTO(dto, itemCatalogo);
        itemCatalogo.setPrestador(prestador);
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
    public Page<ItemCatalogoResponseDTO> buscarItensPorRadarEBusca(String termoBusca, Double lat, Double lon, Double raio, Pageable pageable) {
        Double raioMaximo = (raio != null && raio <= 50.0) ? raio : 10.0;

        // Se vier nulo ou apenas espaços em branco, converte para string vazia
        String termoTratado = (termoBusca == null || termoBusca.trim().isEmpty()) ? "" : termoBusca.trim();

        return itemCatalogoRepository.buscarItensPorTermoEProximidade(termoTratado, lat, lon, raioMaximo, pageable);
    }
}