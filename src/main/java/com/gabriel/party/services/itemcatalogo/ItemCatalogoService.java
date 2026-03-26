package com.gabriel.party.services.itemcatalogo;

import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoRequestDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.mapper.itemcatalogo.ItemCatalogoMapper;
import com.gabriel.party.model.catalogo.ItemCatalogo;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.exceptions.RecursoNaoEncontradoException;
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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador não encontrado com o ID: " + dto.prestadorId()));

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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de catálogo não encontrado com id: " + id));
        return itemCatalogoMapper.toDto(itemCatalogo);
    }

    @Transactional
    public ItemCatalogoResponseDTO atualizarItem(@Valid ItemCatalogoRequestDTO dto, UUID id) {
        var itemCatalogo = itemCatalogoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de catálogo não encontrado com id: " + id));

        var prestador = prestadorRepository.findByIdAndAtivoTrue(dto.prestadorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador não encontrado com o ID: " + dto.prestadorId()));

        itemCatalogoMapper.atualizarItemDoDTO(dto, itemCatalogo);
        itemCatalogo.setPrestador(prestador);
        itemCatalogoRepository.save(itemCatalogo);

        return itemCatalogoMapper.toDto(itemCatalogo);
    }

    @Transactional
    public void deletar(UUID id) {
        var itemCatalogo = itemCatalogoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de catálogo não encontrado com id: " + id));
        itemCatalogo.setAtivo(false);
        itemCatalogoRepository.save(itemCatalogo);
    }
}