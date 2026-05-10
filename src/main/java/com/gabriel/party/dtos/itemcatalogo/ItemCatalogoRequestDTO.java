package com.gabriel.party.dtos.itemcatalogo;

import com.gabriel.party.model.itemcatalogo.enums.TipoItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Dados para criar ou atualizar um item do catálogo do prestador")
public record ItemCatalogoRequestDTO(

        @Schema(description = "Título do anúncio", example = "Buffet Completo para 100 Pessoas")
        @NotBlank(message = "O título do anúncio é obrigatório")
        String titulo,

        @Schema(description = "Descrição detalhada do item", example = "Inclui entrada, prato principal (3 opções), sobremesa e café.")
        String descricao,

        @Schema(description = "Preço base em R$. Pode ser usado como referência de valor mínimo.", example = "3500.00")
        BigDecimal precoBase,

        @Schema(description = "Tipo do item. PRODUTO = bem físico alugado/vendido; SERVICO = mão de obra/serviço prestado; LOCAL = espaço para evento.",
                allowableValues = {"PRODUTO", "SERVICO", "LOCAL"})
        @NotNull(message = "O tipo do item (PRODUTO, SERVICO ou LOCAL) é obrigatório")
        TipoItem tipo,

        @Schema(description = "Detalhes adicionais para itens do tipo LOCAL (capacidade, metragem, etc.). Ignorado para outros tipos.")
        LocalDetalheDTO localDetalhe
) {}
