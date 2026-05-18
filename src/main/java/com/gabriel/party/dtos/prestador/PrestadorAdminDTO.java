package com.gabriel.party.dtos.prestador;

import com.gabriel.party.dtos.prestador.endereco.EnderecoDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO administrativo com dados sensíveis do prestador (CNPJ/CPF, endereço completo,
 * whatsapp). Retornado apenas pelo endpoint /prestadores/{id}/admin restrito a
 * ROLE_ADMINISTRADOR. Não usar em listagens públicas.
 */
@Schema(description = "Dados administrativos completos do prestador (inclui PII). Acesso restrito a admin.")
public record PrestadorAdminDTO(

        @Schema(description = "ID único do prestador")
        UUID id,

        @Schema(description = "Nome completo ou nome fantasia")
        String nome,

        @Schema(description = "E-mail de login (cadastrado em tb_usuarios)")
        String email,

        @Schema(description = "Número de WhatsApp (somente dígitos)")
        String whatsapp,

        @Schema(description = "CPF ou CNPJ — dado sensível, somente admin")
        String cnpjOuCpf,

        @Schema(description = "Descrição livre dos serviços do prestador")
        String descricao,

        @Schema(description = "URL da foto de perfil")
        String fotoPerfilUrl,

        @Schema(description = "Endereço completo com CEP, logradouro, número, complemento, bairro e coordenadas")
        EnderecoDTO endereco,

        @Schema(description = "ID da categoria principal (opcional)")
        UUID categoriaPrincipalId,

        @Schema(description = "Nome da categoria principal (opcional)")
        String categoriaPrincipalNome,

        @Schema(description = "Categorias derivadas dos itens do catálogo")
        List<String> categorias,

        @Schema(description = "Total de itens cadastrados no catálogo")
        Integer totalItens,

        @Schema(description = "Total de avaliações recebidas")
        Integer quantidadeAvaliacoes,

        @Schema(description = "Média das avaliações (1.0 a 5.0). Null se nunca foi avaliado.")
        Double mediaAvaliacoes,

        @Schema(description = "Indica se o prestador está ativo")
        Boolean ativo,

        @Schema(description = "Data em que o usuário foi criado (tb_usuarios.data_criacao)")
        LocalDateTime dataCriacao
) {}
