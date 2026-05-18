package com.gabriel.party.dtos.cliente;

import com.gabriel.party.dtos.prestador.endereco.EnderecoDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO administrativo do cliente. Inclui endereço completo, whatsapp e data
 * de criação. Acesso restrito a ROLE_ADMINISTRADOR via /clientes/{id}/admin.
 */
@Schema(description = "Dados administrativos completos do cliente. Acesso restrito a admin.")
public record ClienteAdminDTO(

        @Schema(description = "ID único do cliente")
        UUID id,

        @Schema(description = "Nome completo")
        String nome,

        @Schema(description = "E-mail de login")
        String email,

        @Schema(description = "Número de WhatsApp (somente dígitos)")
        String whatsapp,

        @Schema(description = "URL da foto de perfil")
        String fotoPerfilUrl,

        @Schema(description = "Endereço completo com CEP, logradouro, número, complemento, bairro e coordenadas")
        EnderecoDTO endereco,

        @Schema(description = "Indica se o cliente está ativo")
        Boolean ativo,

        @Schema(description = "Data em que o usuário foi criado (tb_usuarios.data_criacao)")
        LocalDateTime dataCriacao
) {}
