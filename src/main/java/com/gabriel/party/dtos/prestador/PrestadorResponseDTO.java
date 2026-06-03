package com.gabriel.party.dtos.prestador;

import com.gabriel.party.dtos.avaliacao.AvaliacaoResponseDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.dtos.prestador.endereco.EnderecoDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Perfil completo de um prestador")
public record PrestadorResponseDTO(

        @Schema(description = "ID único do prestador")
        UUID id,

        @Schema(description = "Nome completo ou nome fantasia", example = "Buffet Silva & Cia")
        String nome,

        @Schema(description = "E-mail de contato", example = "contato@buffetsilva.com.br")
        String email,

        @Schema(description = "Número de WhatsApp (somente dígitos)", example = "11987654321")
        String whatsapp,

        @Schema(description = "Lista de categorias derivadas dos itens do catálogo")
        List<CategoriaResumoDTO> categorias,

        @Schema(description = "Descrição dos serviços oferecidos")
        String descricao,

        @Schema(description = "URL da foto de perfil")
        String fotoPerfilUrl,

        @Schema(description = "Endereço do prestador")
        EnderecoDTO endereco,

        @Schema(description = "Itens do catálogo (produtos, serviços e locais) do prestador")
        List<ItemCatalogoResponseDTO> itensCatalogo,

        @Schema(description = "Avaliações recebidas pelo prestador")
        List<AvaliacaoResponseDTO> avaliacoes,

        @Schema(description = "Média das notas das avaliações ativas (1.0 a 5.0). Nulo se não houver avaliações.", example = "4.3")
        Double mediaAvaliacoes,

        @Schema(description = "Total de avaliações ativas recebidas", example = "27")
        Integer quantidadeAvaliacoes,

        @Schema(description = "Data em que o usuário foi criado (tb_usuarios.data_criacao)", example = "2024-05-01T14:30:00")
        LocalDateTime dataCriacao,

        @Schema(description = "Total de pedidos aceitos (concluídos) recebidos pelo prestador", example = "42")
        Long pedidosConcluidos
) {

    @Schema(description = "Resumo de categoria — id e nome")
    public record CategoriaResumoDTO(UUID id, String nome) {}
}
