package com.gabriel.party.services.evento;

import com.gabriel.party.dtos.evento.PrestadorSugestaoDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.evento.EventoMapper;
import com.gabriel.party.model.categoria.Categoria;
import com.gabriel.party.model.evento.Evento;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class SugestaoService {

    private static final int JANELA_RANKING = 10;
    private static final int LIMITE_MAXIMO = 20;

    private final EventoService eventoService;
    private final CategoriaRepository categoriaRepository;
    private final PrestadorRepository prestadorRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final EventoMapper eventoMapper;
    private final Random random = new Random();

    public SugestaoService(EventoService eventoService,
                           CategoriaRepository categoriaRepository,
                           PrestadorRepository prestadorRepository,
                           ItemCatalogoRepository itemCatalogoRepository,
                           EventoMapper eventoMapper) {
        this.eventoService = eventoService;
        this.categoriaRepository = categoriaRepository;
        this.prestadorRepository = prestadorRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.eventoMapper = eventoMapper;
    }

    @Transactional(readOnly = true)
    public List<PrestadorSugestaoDTO> sugerirPrestadores(UUID eventoId,
                                                         String nomeCategoria,
                                                         Integer limite,
                                                         Usuario usuarioLogado) {
        Evento evento = eventoService.buscarEventoDoClienteOuErro(eventoId, usuarioLogado);

        int limiteFinal = (limite == null || limite < 1) ? 3 : Math.min(limite, LIMITE_MAXIMO);

        Categoria categoria = categoriaRepository.findByNomeIgnoreCaseAndAtivoTrue(nomeCategoria)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORIA_NAO_ENCONTRADA, nomeCategoria));

        // Janela maior pra shuffle dentro do top — pega até 30 candidatos da cidade
        Pageable janela = PageRequest.of(0, JANELA_RANKING * 3);

        List<Prestador> candidatos = prestadorRepository.buscarComCategorias(
                List.of(categoria.getId()),
                "",
                emptyOrTrim(evento.getCidade()),
                janela
        ).getContent();

        // Fallback: se cidade vazia de resultados, busca sem filtro de cidade
        if (candidatos.isEmpty() && !emptyOrTrim(evento.getCidade()).isEmpty()) {
            candidatos = prestadorRepository.buscarComCategorias(
                    List.of(categoria.getId()),
                    "",
                    "",
                    janela
            ).getContent();
        }

        if (candidatos.isEmpty()) {
            return Collections.emptyList();
        }

        List<Prestador> ordenados = new ArrayList<>(candidatos);
        ordenados.sort(Comparator
                .comparingDouble(this::mediaAvaliacao).reversed()
                .thenComparing(Prestador::getNomeCompleto));

        // Aplica embaralhamento dentro da janela de top, depois corta no limite
        int tamanhoJanela = Math.min(JANELA_RANKING, ordenados.size());
        List<Prestador> topJanela = new ArrayList<>(ordenados.subList(0, tamanhoJanela));
        Collections.shuffle(topJanela, random);

        List<Prestador> selecionados = topJanela.stream().limit(limiteFinal).toList();

        return selecionados.stream()
                .map(p -> {
                    ItemCatalogo item = itemCatalogoRepository
                            .findFirstByPrestadorIdAndCategoriaIdAndAtivoTrue(p.getId(), categoria.getId())
                            .orElseThrow(() -> new AppException(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO, p.getId().toString()));
                    return eventoMapper.toSugestaoDTO(p, item);
                })
                .toList();
    }

    private double mediaAvaliacao(Prestador prestador) {
        if (prestador.getAvaliacoes() == null || prestador.getAvaliacoes().isEmpty()) {
            return 0.0;
        }
        return prestador.getAvaliacoes().stream()
                .filter(a -> Boolean.TRUE.equals(a.getAtivo()))
                .mapToInt(a -> a.getNota())
                .average()
                .orElse(0.0);
    }

    private String emptyOrTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
