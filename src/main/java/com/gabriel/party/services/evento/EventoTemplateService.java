package com.gabriel.party.services.evento;

import com.gabriel.party.dtos.evento.TipoEventoDTO;
import com.gabriel.party.model.evento.enums.TipoEvento;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class EventoTemplateService {

    /**
     * Mapa de tipo de evento → categorias de serviço sugeridas (nomes como aparecem em tb_categorias).
     * Categorias inexistentes no seed serão silenciosamente ignoradas pela camada de sugestão.
     */
    private static final Map<TipoEvento, List<String>> CATEGORIAS_POR_TIPO = Map.ofEntries(
            Map.entry(TipoEvento.CASAMENTO, List.of(
                    "Buffet", "DJ", "Fotografia", "Decoração", "Cerimonialista", "Bolo", "Espaço para festas")),
            Map.entry(TipoEvento.QUINZE_ANOS, List.of(
                    "Buffet", "DJ", "Fotografia", "Decoração", "Bolo", "Espaço para festas", "Filmagem")),
            Map.entry(TipoEvento.ANIVERSARIO, List.of(
                    "Buffet", "Bolo", "Decoração", "DJ")),
            Map.entry(TipoEvento.INFANTIL, List.of(
                    "Buffet", "Bolo", "Decoração", "Animação infantil", "Brinquedos infláveis")),
            Map.entry(TipoEvento.CHURRASCO, List.of(
                    "Churrasqueiro", "Bebidas", "Espaço para festas")),
            Map.entry(TipoEvento.FORMATURA, List.of(
                    "Buffet", "DJ", "Fotografia", "Decoração", "Espaço para festas")),
            Map.entry(TipoEvento.CORPORATIVO, List.of(
                    "Buffet", "Espaço para festas", "Audiovisual", "Fotografia")),
            Map.entry(TipoEvento.CONFRATERNIZACAO, List.of(
                    "Buffet", "DJ", "Espaço para festas", "Decoração")),
            Map.entry(TipoEvento.CHA_REVELACAO, List.of(
                    "Buffet", "Bolo", "Decoração", "Fotografia")),
            Map.entry(TipoEvento.BATIZADO, List.of(
                    "Buffet", "Bolo", "Decoração", "Fotografia")),
            Map.entry(TipoEvento.OUTRO, Collections.emptyList())
    );

    private static final Map<TipoEvento, String> LABELS = Map.ofEntries(
            Map.entry(TipoEvento.CASAMENTO, "Casamento"),
            Map.entry(TipoEvento.QUINZE_ANOS, "15 Anos"),
            Map.entry(TipoEvento.ANIVERSARIO, "Aniversário"),
            Map.entry(TipoEvento.INFANTIL, "Festa Infantil"),
            Map.entry(TipoEvento.CHURRASCO, "Churrasco"),
            Map.entry(TipoEvento.FORMATURA, "Formatura"),
            Map.entry(TipoEvento.CORPORATIVO, "Corporativo"),
            Map.entry(TipoEvento.CONFRATERNIZACAO, "Confraternização"),
            Map.entry(TipoEvento.CHA_REVELACAO, "Chá Revelação"),
            Map.entry(TipoEvento.BATIZADO, "Batizado"),
            Map.entry(TipoEvento.OUTRO, "Outro")
    );

    public List<TipoEventoDTO> listarTipos() {
        return Arrays.stream(TipoEvento.values())
                .map(tipo -> new TipoEventoDTO(
                        tipo,
                        LABELS.getOrDefault(tipo, tipo.name()),
                        CATEGORIAS_POR_TIPO.getOrDefault(tipo, Collections.emptyList())))
                .toList();
    }

    public List<String> categoriasSugeridas(TipoEvento tipo) {
        return CATEGORIAS_POR_TIPO.getOrDefault(tipo, Collections.emptyList());
    }
}
